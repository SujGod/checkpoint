import { claimsiftApi } from "../api/claimsiftApi";
import { store } from "../redux/store";
import type { FactCheck } from "../types/fact-check";
import type {
  ExtractClaimsResponse,
  ExtractedClaim,
  TranscriptChunk,
} from "../types/transcript";
import { buildVideoManifest, deduplicateClaims } from "../utils/claim.utils";
import { calculateTranscriptDurations } from "../utils/transcript.utils";
import { chunkTranscript } from "./transcript-chunker";
import { readTranscriptFromPage } from "./transcript-reader";

type ShouldContinue = () => boolean;

const processingPromises = new Map<string, Promise<FactCheck[]>>();

const completedManifests = new Map<string, FactCheck[]>();

const sendTranscriptChunkToBackend = async (
  chunk: TranscriptChunk,
): Promise<ExtractClaimsResponse> =>
  store
    .dispatch(
      claimsiftApi.endpoints.extractClaims.initiate({
        videoId: chunk.videoId,
        chunkId: chunk.id,
        text: chunk.text,
        startSeconds: chunk.startSeconds,
        endSeconds: chunk.endSeconds,
      }),
    )
    .unwrap();

const runVideoProcessing = async (
  videoId: string,
  shouldContinue: ShouldContinue,
): Promise<FactCheck[]> => {
  console.log("[ClaimSift] Processing YouTube video:", videoId);

  const rawTranscriptSegments = await readTranscriptFromPage();

  if (!shouldContinue()) {
    console.log(
      "[ClaimSift] Processing stopped because the context is no longer active.",
    );

    return [];
  }

  if (rawTranscriptSegments.length === 0) {
    console.warn("[ClaimSift] Could not find transcript segments on the page.");

    return [];
  }

  const transcriptSegments = calculateTranscriptDurations(
    rawTranscriptSegments,
  );

  const transcriptChunks = chunkTranscript(videoId, transcriptSegments);

  const allClaims: ExtractedClaim[] = [];

  for (const chunk of transcriptChunks) {
    if (!shouldContinue()) {
      console.log(`[ClaimSift] Stopping processing for video ${videoId}.`);

      return [];
    }

    const response = await sendTranscriptChunkToBackend(chunk);

    console.log(
      "[ClaimSift] Extracted claims for chunk:",
      response.chunkId,
      response.claims,
    );

    allClaims.push(...response.claims);
  }

  const uniqueClaims = deduplicateClaims(allClaims);

  const manifest = buildVideoManifest(uniqueClaims);

  console.log("[ClaimSift] Raw extracted claims:", allClaims.length);

  console.log("[ClaimSift] Unique extracted claims:", uniqueClaims.length);

  console.log("[ClaimSift] Generated video manifest:", manifest);

  return manifest;
};

export const processVideoOnce = (
  videoId: string,
  shouldContinue: ShouldContinue,
): Promise<FactCheck[]> => {
  const cachedManifest = completedManifests.get(videoId);

  if (cachedManifest) {
    console.log("[ClaimSift] Reusing completed manifest:", videoId);

    return Promise.resolve(cachedManifest);
  }

  const activePromise = processingPromises.get(videoId);

  if (activePromise) {
    console.log("[ClaimSift] Reusing active processing request:", videoId);

    return activePromise;
  }

  const processingPromise = runVideoProcessing(videoId, shouldContinue)
    .then((manifest) => {
      if (manifest.length > 0) {
        completedManifests.set(videoId, manifest);
      }

      return manifest;
    })
    .catch((error: unknown) => {
      console.error("[ClaimSift] Failed to process video:", videoId, error);

      throw error;
    })
    .finally(() => {
      if (processingPromises.get(videoId) === processingPromise) {
        processingPromises.delete(videoId);
      }
    });

  processingPromises.set(videoId, processingPromise);

  return processingPromise;
};

export const clearCachedVideoManifest = (videoId: string): void => {
  completedManifests.delete(videoId);
};

export const clearAllCachedVideoManifests = (): void => {
  completedManifests.clear();
};

export const extractClaimsFromChunks = async (
  chunks: TranscriptChunk[],
): Promise<ExtractedClaim[]> => {
  const extractedClaims: ExtractedClaim[] = [];

  for (const chunk of chunks) {
    try {
      const response = await store
        .dispatch(
          claimsiftApi.endpoints.extractClaims.initiate({
            videoId: chunk.videoId,
            chunkId: chunk.id,
            text: chunk.text,
            startSeconds: chunk.startSeconds,
            endSeconds: chunk.endSeconds,
          }),
        )
        .unwrap();

      extractedClaims.push(...response.claims);
    } catch (error: unknown) {
      console.error(
        `ClaimSift failed to extract claims from chunk ${chunk.id}:`,
        error,
      );
    }
  }

  return extractedClaims;
};
