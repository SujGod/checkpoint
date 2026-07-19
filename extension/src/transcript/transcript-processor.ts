import { claimsiftApi } from "../api/claimsiftApi";
import { store } from "../redux/store";
import type { FactCheck } from "../types/fact-check";
import type { TranscriptSegment } from "../types/transcript";
import { readTranscriptFromPage } from "./transcript-reader";

type ShouldContinue = () => boolean;

const processingPromises = new Map<string, Promise<FactCheck[]>>();

const completedManifests = new Map<string, FactCheck[]>();

const sendTranscriptToBackend = async (
  videoId: string,
  segments: TranscriptSegment[],
): Promise<FactCheck[]> => {
  const response = await store
    .dispatch(
      claimsiftApi.endpoints.processVideo.initiate({
        videoId,
        segments,
      }),
    )
    .unwrap();

  return response.factChecks;
};

const runVideoProcessing = async (
  videoId: string,
  shouldContinue: ShouldContinue,
): Promise<FactCheck[]> => {
  const transcriptSegments = await readTranscriptFromPage();

  if (!shouldContinue() || transcriptSegments.length === 0) {
    return [];
  }

  const manifest = await sendTranscriptToBackend(videoId, transcriptSegments);

  if (!shouldContinue()) {
    return [];
  }

  return manifest;
};

export const processVideoOnce = (
  videoId: string,
  shouldContinue: ShouldContinue,
): Promise<FactCheck[]> => {
  const cachedManifest = completedManifests.get(videoId);

  if (cachedManifest) {
    return Promise.resolve(cachedManifest);
  }

  const activePromise = processingPromises.get(videoId);

  if (activePromise) {
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
