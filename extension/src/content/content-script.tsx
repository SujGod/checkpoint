import { isFactCheckEnabled } from "../storage/settings";
import {
  disableClaimSift,
  enableClaimSift,
  resetClaimSift,
} from "./claimsift-controller";
import { insertFactCheckToggle } from "./fact-check-toggle";
import { getCurrentYouTubeVideoId } from "./youtube-video-controller";
import "./fact-check-toggle.css";

console.log("[ClaimSift] Content script loaded.");

const NAVIGATION_SETTLE_DELAY_MS = 500;

let lastVideoId: string | null = null;
let navigationTimeoutId: number | undefined;
let navigationGeneration = 0;

const wait = (milliseconds: number): Promise<void> =>
  new Promise((resolve) => {
    window.setTimeout(resolve, milliseconds);
  });

const insertToggle = async (): Promise<void> => {
  await insertFactCheckToggle({
    onEnable: async () => {
      await enableClaimSift();
    },

    onDisable: () => {
      disableClaimSift();
    },
  });
};

const initialize = async (): Promise<void> => {
  await insertToggle();

  const videoId = getCurrentYouTubeVideoId();

  lastVideoId = videoId;

  if (await isFactCheckEnabled()) {
    await enableClaimSift();
  }
};

const handleYouTubeNavigation = async (): Promise<void> => {
  const generation = ++navigationGeneration;

  /*
   * YouTube fires yt-navigate-finish before every part of the new
   * watch page is necessarily available. Give the player, controls,
   * description, and transcript button time to render.
   */
  await wait(NAVIGATION_SETTLE_DELAY_MS);

  /*
   * Another navigation occurred while this handler was waiting.
   * Ignore this outdated run.
   */
  if (generation !== navigationGeneration) {
    return;
  }

  const currentVideoId = getCurrentYouTubeVideoId();

  console.log("[ClaimSift] YouTube navigation settled:", {
    previousVideoId: lastVideoId,
    currentVideoId,
  });

  /*
   * YouTube may replace the player controls during SPA navigation.
   * Calling this again is safe because insertFactCheckToggle already
   * prevents duplicate buttons.
   */
  await insertToggle();

  /*
   * Do not restart processing for navigation events that did not
   * actually change the current video.
   */
  if (!currentVideoId || currentVideoId === lastVideoId) {
    return;
  }

  lastVideoId = currentVideoId;

  const enabled = await isFactCheckEnabled();

  if (!enabled) {
    /*
     * Keep the toggle available, but do not open the transcript
     * until the user enables ClaimSift.
     */
    disableClaimSift();
    return;
  }

  console.log(
    "[ClaimSift] ClaimSift remainsok niceenabled; processing new video:",
    currentVideoId,
  );

  /*
   * resetClaimSift should:
   * - clear the previous manifest and overlay
   * - detach from the previous video element
   * - attach to the new video
   * - call enableClaimSift()
   * - open the new transcript
   * - request claims for the new video
   */
  await resetClaimSift();
};

const scheduleNavigationHandling = (): void => {
  if (navigationTimeoutId !== undefined) {
    window.clearTimeout(navigationTimeoutId);
  }

  /*
   * Debounce repeated YouTube navigation events.
   */
  navigationTimeoutId = window.setTimeout(() => {
    navigationTimeoutId = undefined;

    void handleYouTubeNavigation().catch((error: unknown) => {
      console.error("[ClaimSift] Navigation handling failed:", error);
    });
  }, 150);
};

document.addEventListener("yt-navigate-finish", scheduleNavigationHandling);

void initialize().catch((error: unknown) => {
  console.error("[ClaimSift] Initialization failed:", error);
});
