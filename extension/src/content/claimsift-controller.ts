import type { FactCheck } from "../types/fact-check";
import {
  removeClaimSiftOverlay,
  renderClaimSiftOverlay,
} from "./overlay-controller";
import { processVideoOnce } from "../transcript/transcript-processor";
import {
  waitForYouTubeVideo,
  getCurrentYouTubeVideoId,
} from "./youtube-video-controller";

let enabled = false;
let manifest: FactCheck[] = [];
let activeFactCheck: FactCheck | null = null;
let video: HTMLVideoElement | null = null;

const findActiveFactCheck = (currentTime: number): FactCheck | null =>
  manifest.find(
    (factCheck) =>
      currentTime >= factCheck.startSeconds &&
      currentTime <= factCheck.endSeconds,
  ) ?? null;

const handleTimeUpdate = (): void => {
  if (!enabled || !video) {
    return;
  }

  const next = findActiveFactCheck(video.currentTime);

  if (next?.id === activeFactCheck?.id) {
    return;
  }

  activeFactCheck = next;
  renderClaimSiftOverlay(next);
};

const attachVideo = (nextVideo: HTMLVideoElement): void => {
  video?.removeEventListener("timeupdate", handleTimeUpdate);

  video = nextVideo;

  video.addEventListener("timeupdate", handleTimeUpdate);
};

export const enableClaimSift = async (): Promise<void> => {
  enabled = true;

  video = await waitForYouTubeVideo();
  attachVideo(video);

  const videoId = getCurrentYouTubeVideoId();

  if (!videoId) {
    return;
  }

  manifest = await processVideoOnce(
    videoId,
    () => enabled && getCurrentYouTubeVideoId() === videoId,
  );

  handleTimeUpdate();
};

export const disableClaimSift = (): void => {
  enabled = false;

  video?.removeEventListener("timeupdate", handleTimeUpdate);

  activeFactCheck = null;
  manifest = [];

  removeClaimSiftOverlay();
};

export const resetClaimSift = async (): Promise<void> => {
  video?.removeEventListener("timeupdate", handleTimeUpdate);

  video = null;
  manifest = [];
  activeFactCheck = null;

  removeClaimSiftOverlay();

  if (enabled) {
    await enableClaimSift();
  }
};
