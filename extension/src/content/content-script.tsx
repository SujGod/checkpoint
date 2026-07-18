// import type { FactCheck } from "../types/fact-check";
// import type {
//   ExtractClaimsResponse,
//   ExtractedClaim,
//   TranscriptChunk,
// } from "../types/transcript";

import { isFactCheckEnabled } from "../storage/settings";
import {
  enableClaimSift,
  disableClaimSift,
  resetClaimSift,
} from "./claimsift-controller";
import { insertFactCheckToggle } from "./fact-check-toggle";
import "./fact-check-toggle.css";

// import { buildVideoManifest, deduplicateClaims } from "../utils/claim.utils";
// import { store } from "../redux/store";

// import { isFactCheckEnabled } from "../storage/settings";
// import { insertFactCheckToggle } from "./fact-check-toggle";
// import { readTranscriptFromPage } from "../transcript/transcript-reader";
// import { calculateTranscriptDurations } from "../utils/transcript.utils";
// import { chunkTranscript } from "../transcript/transcript-chunker";

// import { claimsiftApi } from "../api/claimsiftApi";

// import "./fact-check-toggle.css";
// import {
//   removeClaimSiftOverlay,
//   renderClaimSiftOverlay,
// } from "./overlay-controller";
// import { processVideoOnce } from "../transcript/transcript-processor";
// import { disableClaimSift, enableClaimSift, resetClaimSift } from "./claimsift-controller";

// let activeFactCheck: FactCheck | null = null;
// let attachedVideo: HTMLVideoElement | null = null;
// let videoDiscoveryObserver: MutationObserver | null = null;

// let factCheckingEnabled = false;
// let toggleInitialized = false;
// let initializationPromise: Promise<void> | null = null;

// let videoManifest: FactCheck[] = [];

/**
 * Stores the active processing promise for each video.
 *
 * If multiple code paths request processing for the same video,
 * they all receive the same promise instead of starting duplicate work.
 */
// const processingPromises = new Map<string, Promise<void>>();

/**
 * Stores videos that completed successfully during this page session.
 */
// const completedVideoIds = new Set<string>();

// console.log("[ClaimSift] Content script loaded.");

// const getYouTubeVideoId = (): string | null => {
//   const url = new URL(window.location.href);

//   return url.searchParams.get("v");
// };

// const sendTranscriptChunkToBackend = async (
//   chunk: TranscriptChunk,
// ): Promise<ExtractClaimsResponse> => {
//   const request = store.dispatch(
//     claimsiftApi.endpoints.extractClaims.initiate({
//       videoId: chunk.videoId,
//       chunkId: chunk.id,
//       text: chunk.text,
//       startSeconds: chunk.startSeconds,
//       endSeconds: chunk.endSeconds,
//     }),
//   );

//   return request.unwrap();
// };

// const runVideoProcessing = async (videoId: string): Promise<void> => {
//   console.log("[ClaimSift] Processing YouTube video:", videoId);

//   const rawTranscriptSegments = await readTranscriptFromPage();

//   if (!factCheckingEnabled) {
//     console.log(
//       "[ClaimSift] Processing stopped because fact checking was disabled.",
//     );

//     return;
//   }

//   if (rawTranscriptSegments.length === 0) {
//     console.warn("[ClaimSift] Could not find transcript segments on the page.");

//     return;
//   }

//   const transcriptSegments = calculateTranscriptDurations(
//     rawTranscriptSegments,
//   );

//   const transcriptChunks = chunkTranscript(videoId, transcriptSegments);

//   console.log("[ClaimSift] Transcript chunks:", transcriptChunks);

//   const allClaims: ExtractedClaim[] = [];

//   for (const chunk of transcriptChunks) {
//     if (!factCheckingEnabled) {
//       console.log(
//         "[ClaimSift] Processing stopped because fact checking was disabled.",
//       );

//       return;
//     }

//     if (getYouTubeVideoId() !== videoId) {
//       console.log(
//         `[ClaimSift] Stopping processing for ${videoId} because the active video changed.`,
//       );

//       return;
//     }

//     const response = await sendTranscriptChunkToBackend(chunk);

//     console.log(
//       "[ClaimSift] Extracted claims for chunk:",
//       chunk.id,
//       response.claims,
//     );

//     allClaims.push(...response.claims);
//   }

//   const uniqueClaims = deduplicateClaims(allClaims);

//   videoManifest = buildVideoManifest(uniqueClaims);

//   console.log("[ClaimSift] Raw extracted claims:", allClaims.length);

//   console.log("[ClaimSift] Unique extracted claims:", uniqueClaims.length);

//   console.log("[ClaimSift] Generated video manifest:", videoManifest);

//   if (factCheckingEnabled && getYouTubeVideoId() === videoId) {
//     completedVideoIds.add(videoId);

//     handleTimeUpdate();

//     console.log("[ClaimSift] Finished processing video:", videoId);
//   }
// };

// const processCurrentVideo = (): Promise<void> => {
//   if (!factCheckingEnabled) {
//     return Promise.resolve();
//   }

//   const videoId = getYouTubeVideoId();

//   if (!videoId) {
//     console.warn(
//       "[ClaimSift] Could not determine the current YouTube video ID.",
//     );

//     return Promise.resolve();
//   }

//   if (completedVideoIds.has(videoId)) {
//     console.log("[ClaimSift] Skipping already completed video:", videoId);

//     return Promise.resolve();
//   }

//   const existingPromise = processingPromises.get(videoId);

//   if (existingPromise) {
//     console.log("[ClaimSift] Reusing active processing request:", videoId);

//     return existingPromise;
//   }

//   const processingPromise = processVideoOnce(videoId)
//     .catch((error: unknown) => {
//       console.error("[ClaimSift] Failed to process the current video:", error);

//       throw error;
//     })
//     .finally(() => {
//       /**
//        * Only delete this exact promise. This prevents an older promise
//        * from accidentally deleting a newer processing request.
//        */
//       if (processingPromises.get(videoId) === processingPromise) {
//         processingPromises.delete(videoId);
//       }
//     });

//   processingPromises.set(videoId, processingPromise);

//   return processingPromise;
// };

// const getVideo = (): HTMLVideoElement | null =>
//   document.querySelector<HTMLVideoElement>("video");

// const createOverlayContainer = (): HTMLDivElement => {
//   const existingContainer =
//     document.querySelector<HTMLDivElement>("#claimsift-root");

//   if (existingContainer) {
//     existingContainer.remove();
//   }

//   const player =
//     document.querySelector<HTMLElement>("#movie_player") ?? document.body;

//   const container = document.createElement("div");

//   container.id = "claimsift-root";

//   if (player !== document.body) {
//     const position = window.getComputedStyle(player).position;

//     if (position === "static") {
//       player.style.position = "relative";
//     }
//   }

//   player.appendChild(container);

//   overlayContainer = container;
//   root = createRoot(container);

//   return container;
// };

// const removeClaimSiftOverlay = (): void => {
//   activeFactCheck = null;

//   root?.unmount();
//   overlayContainer?.remove();

//   root = null;
//   overlayContainer = null;
// };

// const renderFactCheck = (factCheck: FactCheck | null): void => {
//   if (!factCheckingEnabled) {
//     return;
//   }

//   if (!factCheck) {
//     root?.render(null);
//     return;
//   }

//   if (!root || !overlayContainer) {
//     createOverlayContainer();
//   }

//   if (!root) {
//     return;
//   }

//   console.log(
//     "[ClaimSift] Mounting overlay container for claim:",
//     factCheck.claim,
//   );

//   root.render(
//     <Provider store={store}>
//       <FactCheckOverlayContainer key={factCheck.id} claim={factCheck.claim} />
//     </Provider>,
//   );
// };

// const findActiveFactCheck = (currentTime: number): FactCheck | null =>
//   videoManifest.find(
//     (factCheck) =>
//       currentTime >= factCheck.startSeconds &&
//       currentTime <= factCheck.endSeconds,
//   ) ?? null;

// const handleTimeUpdate = (): void => {
//   if (!factCheckingEnabled || !attachedVideo) {
//     return;
//   }

//   const nextFactCheck = findActiveFactCheck(attachedVideo.currentTime);

//   if (nextFactCheck?.id !== activeFactCheck?.id) {
//     activeFactCheck = nextFactCheck;
//     renderClaimSiftOverlay(activeFactCheck);
//   }
// };

// const detachFromCurrentVideo = (): void => {
//   attachedVideo?.removeEventListener("timeupdate", handleTimeUpdate);

//   attachedVideo = null;
// };

// const attachToVideo = (video: HTMLVideoElement): void => {
//   if (attachedVideo === video) {
//     handleTimeUpdate();
//     return;
//   }

//   detachFromCurrentVideo();

//   attachedVideo = video;

//   if (factCheckingEnabled) {
//     attachedVideo.addEventListener("timeupdate", handleTimeUpdate);

//     handleTimeUpdate();
//   }
// };

// const startFactChecking = async (): Promise<void> => {
//   /**
//    * The toggle callback may fire more than once. Calling this while
//    * already enabled should still ensure the current video is processed,
//    * but it must not attach another timeupdate listener.
//    */
//   if (!factCheckingEnabled) {
//     factCheckingEnabled = true;

//     attachedVideo?.addEventListener("timeupdate", handleTimeUpdate);
//   }

//   await processVideoOnce();

//   handleTimeUpdate();
// };

// const stopFactChecking = (): void => {
//   if (!factCheckingEnabled) {
//     return;
//   }

//   factCheckingEnabled = false;

//   attachedVideo?.removeEventListener("timeupdate", handleTimeUpdate);

//   removeClaimSiftOverlay();
// };

// const initializeToggle = async (): Promise<void> => {
//   if (toggleInitialized) {
//     return;
//   }

//   toggleInitialized = true;

//   try {
//     await insertFactCheckToggle({
//       onEnable: () => {
//         void startFactChecking().catch((error: unknown) => {
//           console.error("[ClaimSift] Could not start fact checking:", error);
//         });
//       },

//       onDisable: stopFactChecking,
//     });
//   } catch (error) {
//     /**
//      * Permit another initialization attempt if insertion failed.
//      */
//     toggleInitialized = false;
//     throw error;
//   }
// };

// const stopVideoDiscoveryObserver = (): void => {
//   videoDiscoveryObserver?.disconnect();
//   videoDiscoveryObserver = null;
// };

// const waitForVideo = (): Promise<HTMLVideoElement> =>
//   new Promise((resolve) => {
//     const existingVideo = getVideo();

//     if (existingVideo) {
//       resolve(existingVideo);
//       return;
//     }

//     stopVideoDiscoveryObserver();

//     videoDiscoveryObserver = new MutationObserver(() => {
//       const discoveredVideo = getVideo();

//       if (!discoveredVideo) {
//         return;
//       }

//       stopVideoDiscoveryObserver();
//       resolve(discoveredVideo);
//     });

//     videoDiscoveryObserver.observe(document.body, {
//       childList: true,
//       subtree: true,
//     });
//   });

// const runInitialization = async (): Promise<void> => {
//   stopVideoDiscoveryObserver();

//   factCheckingEnabled = await isFactCheckEnabled();

//   await initializeToggle();

//   const video = await waitForVideo();

//   attachToVideo(video);

//   if (factCheckingEnabled) {
//     await processCurrentVideo();
//     handleTimeUpdate();
//   }
// };

// const initialize = (): Promise<void> => {
//   /**
//    * Several YouTube events can call initialize close together.
//    * Reuse one initialization promise instead of running concurrently.
//    */
//   if (initializationPromise) {
//     return initializationPromise;
//   }

//   initializationPromise = runInitialization()
//     .catch((error: unknown) => {
//       console.error("[ClaimSift] Initialization failed:", error);

//       throw error;
//     })
//     .finally(() => {
//       initializationPromise = null;
//     });

//   return initializationPromise;
// };

// const resetForNavigation = (): void => {
//   stopVideoDiscoveryObserver();
//   detachFromCurrentVideo();
//   removeClaimSiftOverlay();

//   activeFactCheck = null;
//   videoManifest = [];

//   /**
//    * Do not clear completedVideoIds here.
//    *
//    * Keeping it means navigating away from a video and then returning to it
//    * will not process it again during the same page session.
//    *
//    * Remove the active promise for the previous video only after it settles;
//    * runVideoProcessing also checks whether the active video changed.
//    */
// };

// const handleYouTubeNavigation = (): void => {
//   const videoId = getYouTubeVideoId();

//   console.log("[ClaimSift] YouTube navigation completed:", videoId);

//   resetForNavigation();

//   void initialize().catch((error: unknown) => {
//     console.error(
//       "[ClaimSift] Could not initialize after YouTube navigation:",
//       error,
//     );
//   });
// };

// /**
//  * A content script is normally evaluated once per page document, but this
//  * marker also prevents duplicate listeners if the script is injected again.
//  */
// const navigationListenerKey = "__claimsiftNavigationListenerInstalled";

// type ClaimSiftWindow = Window & {
//   [navigationListenerKey]?: boolean;
// };

// const claimSiftWindow = window as ClaimSiftWindow;

// if (!claimSiftWindow[navigationListenerKey]) {
//   claimSiftWindow[navigationListenerKey] = true;

//   document.addEventListener("yt-navigate-finish", handleYouTubeNavigation);
// }

// void initialize().catch((error: unknown) => {
//   console.error("[ClaimSift] Initial initialization failed:", error);
// });

console.log("[ClaimSift] Content script loaded.");

const initialize = async (): Promise<void> => {
  await insertFactCheckToggle({
    onEnable: () => {
      void enableClaimSift();
    },
    onDisable: disableClaimSift,
  });

  if (await isFactCheckEnabled()) {
    await enableClaimSift();
  }
};

document.addEventListener("yt-navigate-finish", () => {
  void resetClaimSift();
});

void initialize().catch((error: unknown) => {
  console.error("[ClaimSift] Initialization failed:", error);
});
