import claimSiftLogo from "../assets/claimSiftLogo.png";
import type { FactCheck } from "../types/fact-check";

const MARKERS_CONTAINER_ID =
  "claimsift-progress-markers";

const PROGRESS_BAR_SELECTORS = [
  ".ytp-progress-bar-container",
  ".ytp-progress-bar",
];

const VIDEO_SELECTOR =
  "video.html5-main-video";

let currentFactChecks: FactCheck[] = [];

const getYouTubeVideo =
  (): HTMLVideoElement | null =>
    document.querySelector<HTMLVideoElement>(
      VIDEO_SELECTOR,
    );

const getProgressBar =
  (): HTMLElement | null => {
    for (const selector of PROGRESS_BAR_SELECTORS) {
      const progressBar =
        document.querySelector<HTMLElement>(
          selector,
        );

      if (progressBar) {
        return progressBar;
      }
    }

    return null;
  };

const calculatePositionPercentage = (
  timestampSeconds: number,
  videoDurationSeconds: number,
): number => {
  if (
    !Number.isFinite(timestampSeconds) ||
    !Number.isFinite(videoDurationSeconds) ||
    videoDurationSeconds <= 0
  ) {
    return 0;
  }

  const positionPercentage =
    (timestampSeconds / videoDurationSeconds) *
    100;

  return Math.min(
    Math.max(positionPercentage, 0),
    100,
  );
};

const createMarker = (
  factCheck: FactCheck,
  videoDuration: number,
): HTMLSpanElement => {
  const marker =
    document.createElement("span");

  marker.className =
    "claimsift-progress-marker";

  marker.dataset.factCheckId =
    factCheck.id;

  marker.style.left =
    `${calculatePositionPercentage(
      factCheck.startSeconds,
      videoDuration,
    )}%`;

  const logo =
    document.createElement("img");

  logo.className =
    "claimsift-progress-marker-logo";

  logo.src = chrome.runtime.getURL(claimSiftLogo);
  logo.alt = "";
  logo.draggable = false;

  marker.appendChild(logo);

  return marker;
};

const renderMarkersNow = (): boolean => {
  removeClaimSiftProgressMarkers();

  if (currentFactChecks.length === 0) {
    return true;
  }

  const video = getYouTubeVideo();
  const progressBar = getProgressBar();

  if (!video || !progressBar) {
    return false;
  }

  if (
    !Number.isFinite(video.duration) ||
    video.duration <= 0
  ) {
    return false;
  }

  const markersContainer =
    document.createElement("div");

  markersContainer.id =
    MARKERS_CONTAINER_ID;

  for (const factCheck of currentFactChecks) {
    markersContainer.appendChild(
      createMarker(
        factCheck,
        video.duration,
      ),
    );
  }

  progressBar.appendChild(
    markersContainer,
  );

  console.log(
    `[ClaimSift] Rendered ${currentFactChecks.length} progress markers.`,
  );

  return true;
};

const renderMarkersWhenReady =
  (): void => {
    if (renderMarkersNow()) {
      return;
    }

    const video = getYouTubeVideo();

    if (video) {
      video.addEventListener(
        "loadedmetadata",
        () => {
          renderMarkersNow();
        },
        { once: true },
      );
    }

    window.setTimeout(
      () => {
        renderMarkersNow();
      },
      500,
    );
  };

export const renderClaimSiftProgressMarkers = (
  factChecks: FactCheck[],
): void => {
  currentFactChecks = [...factChecks];

  renderMarkersWhenReady();
};

export const rerenderClaimSiftProgressMarkers =
  (): void => {
    if (currentFactChecks.length === 0) {
      return;
    }

    renderMarkersWhenReady();
  };

export const removeClaimSiftProgressMarkers =
  (): void => {
    document
      .getElementById(
        MARKERS_CONTAINER_ID,
      )
      ?.remove();
  };

export const resetClaimSiftProgressMarkers =
  (): void => {
    currentFactChecks = [];

    removeClaimSiftProgressMarkers();
  };