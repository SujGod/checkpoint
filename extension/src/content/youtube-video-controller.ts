type TimeUpdateHandler = (video: HTMLVideoElement) => void;

let attachedVideo: HTMLVideoElement | null = null;
let observer: MutationObserver | null = null;

export const getCurrentYouTubeVideoId = (): string | null =>
  new URL(location.href).searchParams.get("v");

export const getAttachedVideo = (): HTMLVideoElement | null => attachedVideo;

export const waitForYouTubeVideo = (): Promise<HTMLVideoElement> =>
  new Promise((resolve) => {
    const existing = document.querySelector<HTMLVideoElement>("video");

    if (existing) {
      resolve(existing);
      return;
    }

    observer?.disconnect();

    observer = new MutationObserver(() => {
      const video = document.querySelector<HTMLVideoElement>("video");

      if (!video) {
        return;
      }

      observer?.disconnect();
      observer = null;
      resolve(video);
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
    });
  });

export const attachVideoListener = (
  video: HTMLVideoElement,
  handler: TimeUpdateHandler,
): void => {
  detachVideoListener(handler);

  attachedVideo = video;

  attachedVideo.addEventListener("timeupdate", () => handler(video));
};

export const detachVideoListener = (handler: TimeUpdateHandler): void => {
  if (!attachedVideo) {
    return;
  }

  attachedVideo.removeEventListener("timeupdate", () =>
    handler(attachedVideo!),
  );

  attachedVideo = null;
};
