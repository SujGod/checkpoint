import { createRoot, type Root } from "react-dom/client";
import { Provider } from "react-redux";

import type { FactCheck } from "../types/fact-check";
import { videoManifest } from "../video-manifest";
import { store } from "../redux/store";
import FactCheckOverlayContainer from "../components/FactCheckOverlay/FactCheckOverlayContainer";

import { isFactCheckEnabled } from "../storage/settings";
import { insertFactCheckToggle } from "./fact-check-toggle";

import "./fact-check-toggle.css";

let root: Root | null = null;
let overlayContainer: HTMLDivElement | null = null;
let activeFactCheck: FactCheck | null = null;
let attachedVideo: HTMLVideoElement | null = null;
let videoDiscoveryObserver: MutationObserver | null = null;

let factCheckingEnabled = false;

const getVideo = (): HTMLVideoElement | null => {
  return document.querySelector<HTMLVideoElement>("video");
};

const createOverlayContainer = (): HTMLDivElement => {
  const existingContainer =
    document.querySelector<HTMLDivElement>("#check-point-root");

  if (existingContainer) {
    existingContainer.remove();
  }

  const player =
    document.querySelector<HTMLElement>("#movie_player") ?? document.body;

  const container = document.createElement("div");
  container.id = "check-point-root";

  if (player !== document.body) {
    const position = window.getComputedStyle(player).position;

    if (position === "static") {
      player.style.position = "relative";
    }
  }

  player.appendChild(container);

  overlayContainer = container;
  root = createRoot(container);

  return container;
};

const removeClaimSiftOverlay = (): void => {
  activeFactCheck = null;

  root?.unmount();
  overlayContainer?.remove();

  root = null;
  overlayContainer = null;
};

const renderFactCheck = (factCheck: FactCheck | null): void => {
  if (!factCheckingEnabled) {
    return;
  }

  if (!factCheck) {
    root?.render(null);
    return;
  }

  if (!root || !overlayContainer) {
    createOverlayContainer();
  }

  if (!root) {
    return;
  }

  console.log("Rendering ClaimSift claim:", factCheck.id, factCheck.claim);

  root.render(
    <Provider store={store}>
      <FactCheckOverlayContainer key={factCheck.id} claim={factCheck.claim} />
    </Provider>,
  );
};

const findActiveFactCheck = (currentTime: number): FactCheck | null => {
  return (
    videoManifest.find(
      (factCheck) =>
        currentTime >= factCheck.startSeconds &&
        currentTime <= factCheck.endSeconds,
    ) ?? null
  );
};

const handleTimeUpdate = (): void => {
  if (!factCheckingEnabled || !attachedVideo) {
    return;
  }

  const nextFactCheck = findActiveFactCheck(attachedVideo.currentTime);

  if (nextFactCheck?.id !== activeFactCheck?.id) {
    console.log("ClaimSift active fact check changed:", nextFactCheck);

    activeFactCheck = nextFactCheck;
    renderFactCheck(activeFactCheck);
  }
};

const attachToVideo = (video: HTMLVideoElement): void => {
  if (attachedVideo === video) {
    handleTimeUpdate();
    return;
  }

  if (attachedVideo) {
    attachedVideo.removeEventListener("timeupdate", handleTimeUpdate);
  }

  attachedVideo = video;

  if (factCheckingEnabled) {
    attachedVideo.addEventListener("timeupdate", handleTimeUpdate);

    handleTimeUpdate();
  }
};

const startFactChecking = (): void => {
  if (factCheckingEnabled) {
    return;
  }

  factCheckingEnabled = true;

  if (attachedVideo) {
    attachedVideo.addEventListener("timeupdate", handleTimeUpdate);

    handleTimeUpdate();
  }
};

const stopFactChecking = (): void => {
  if (!factCheckingEnabled) {
    return;
  }

  factCheckingEnabled = false;

  attachedVideo?.removeEventListener("timeupdate", handleTimeUpdate);

  removeClaimSiftOverlay();
};

const initializeToggle = async (): Promise<void> => {
  await insertFactCheckToggle({
    onEnable: startFactChecking,
    onDisable: stopFactChecking,
  });
};

const initialize = async (): Promise<void> => {
  videoDiscoveryObserver?.disconnect();
  videoDiscoveryObserver = null;

  factCheckingEnabled = await isFactCheckEnabled();

  await initializeToggle();

  const video = getVideo();

  if (video) {
    attachToVideo(video);

    if (factCheckingEnabled) {
      handleTimeUpdate();
    }

    return;
  }

  videoDiscoveryObserver = new MutationObserver(async () => {
    await initializeToggle();

    const discoveredVideo = getVideo();

    if (discoveredVideo) {
      videoDiscoveryObserver?.disconnect();
      videoDiscoveryObserver = null;

      attachToVideo(discoveredVideo);
    }
  });

  videoDiscoveryObserver.observe(document.body, {
    childList: true,
    subtree: true,
  });
};

document.addEventListener("yt-navigate-finish", async () => {
  attachedVideo?.removeEventListener("timeupdate", handleTimeUpdate);

  videoDiscoveryObserver?.disconnect();
  videoDiscoveryObserver = null;

  removeClaimSiftOverlay();

  activeFactCheck = null;
  attachedVideo = null;

  await initialize();
});

void initialize();