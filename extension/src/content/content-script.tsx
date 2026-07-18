import { createRoot, type Root } from "react-dom/client";
import type { FactCheck } from "../types/fact-check";
import { videoManifest } from "../video-manifest";
import { Provider } from "react-redux";
import { store } from "../redux/store";
import FactCheckOverlayContainer from "../components/FactCheckOverlay/FactCheckOverlayContainer";

let root: Root | null = null;
let overlayContainer: HTMLDivElement | null = null;
let activeFactCheck: FactCheck | null = null;
let attachedVideo: HTMLVideoElement | null = null;
let videoDiscoveryObserver: MutationObserver | null = null;

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

const renderFactCheck = (factCheck: FactCheck | null): void => {
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

  console.log("Rendering CheckPoint claim:", factCheck.id, factCheck.claim);

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
  if (!attachedVideo) {
    return;
  }

  const nextFactCheck = findActiveFactCheck(attachedVideo.currentTime);

  if (nextFactCheck?.id !== activeFactCheck?.id) {
    console.log("CheckPoint active fact check changed:", nextFactCheck);
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
  attachedVideo.addEventListener("timeupdate", handleTimeUpdate);

  handleTimeUpdate();
};

const initialize = (): void => {
  videoDiscoveryObserver?.disconnect();
  videoDiscoveryObserver = null;

  const video = getVideo();

  if (video) {
    createOverlayContainer();
    attachToVideo(video);
    return;
  }

  videoDiscoveryObserver = new MutationObserver(() => {
    const discoveredVideo = getVideo();

    if (discoveredVideo) {
      videoDiscoveryObserver?.disconnect();
      videoDiscoveryObserver = null;
      createOverlayContainer();
      attachToVideo(discoveredVideo);
    }
  });

  videoDiscoveryObserver.observe(document.body, {
    childList: true,
    subtree: true,
  });
};

document.addEventListener("yt-navigate-finish", () => {
  if (attachedVideo) {
    attachedVideo.removeEventListener("timeupdate", handleTimeUpdate);
  }

  videoDiscoveryObserver?.disconnect();
  videoDiscoveryObserver = null;

  root?.unmount();
  overlayContainer?.remove();

  root = null;
  overlayContainer = null;
  activeFactCheck = null;
  attachedVideo = null;

  initialize();
});

initialize();
