import type { TranscriptSegment } from "../types/transcript";
import { parseTimestamp } from "../utils/transcript.utils";

const TRANSCRIPT_PANEL_SELECTORS = [
  'ytd-engagement-panel-section-list-renderer[target-id="engagement-panel-searchable-transcript"]',
  'ytd-engagement-panel-section-list-renderer[target-id="PAmodern_transcript_view"]',
  'ytd-engagement-panel-section-list-renderer[visibility="ENGAGEMENT_PANEL_VISIBILITY_EXPANDED"]',
];

const TRANSCRIPT_PANEL_SELECTOR = TRANSCRIPT_PANEL_SELECTORS.join(",");

const EXACT_TIMESTAMP_PATTERN = /^(?:\d{1,2}:)?\d{1,2}:\d{2}$/;

const DURATION_LABEL_PATTERN =
  /^\d+\s+(?:second|seconds|minute|minutes|hour|hours)$/i;

const wait = (milliseconds: number): Promise<void> =>
  new Promise((resolve) => {
    window.setTimeout(resolve, milliseconds);
  });

const getTranscriptPanel = (): HTMLElement | null => {
  const panels = Array.from(
    document.querySelectorAll<HTMLElement>(TRANSCRIPT_PANEL_SELECTOR),
  );

  return (
    panels.find((panel) => {
      const targetId = panel.getAttribute("target-id") ?? "";

      const visibility = panel.getAttribute("visibility");

      const isTranscriptPanel =
        targetId === "engagement-panel-searchable-transcript" ||
        targetId === "PAmodern_transcript_view" ||
        panel.querySelector("ytd-transcript-segment-renderer") !== null ||
        panel.innerText.toLowerCase().includes("transcript");

      const isExpanded =
        visibility === "ENGAGEMENT_PANEL_VISIBILITY_EXPANDED" ||
        isElementVisible(panel);

      return isTranscriptPanel && isExpanded;
    }) ?? null
  );
};

const isElementVisible = (element: HTMLElement): boolean => {
  const styles = window.getComputedStyle(element);
  const rectangle = element.getBoundingClientRect();

  return (
    styles.display !== "none" &&
    styles.visibility !== "hidden" &&
    rectangle.width > 0 &&
    rectangle.height > 0
  );
};

const isTranscriptPanelOpen = (): boolean => {
  const panel = getTranscriptPanel();

  if (!panel) {
    return false;
  }

  const visibility = panel.getAttribute("visibility");

  const hasSegments =
    panel.querySelector("ytd-transcript-segment-renderer") !== null;

  return (
    visibility === "ENGAGEMENT_PANEL_VISIBILITY_EXPANDED" ||
    hasSegments ||
    isElementVisible(panel)
  );
};

const findTranscriptButtonInSection = (): HTMLButtonElement | null => {
  const sections = Array.from(
    document.querySelectorAll<HTMLElement>(
      [
        "ytd-video-description-transcript-section-renderer",
        '[class*="transcript-section"]',
      ].join(","),
    ),
  );

  for (const section of sections) {
    const button =
      section.querySelector<HTMLButtonElement>("yt-button-shape button") ??
      section.querySelector<HTMLButtonElement>("button");

    if (button) {
      return button;
    }
  }

  return null;
};

const findTranscriptButtonByText = (): HTMLButtonElement | null => {
  const buttons = Array.from(
    document.querySelectorAll<HTMLButtonElement>("button"),
  );

  return (
    buttons.find((button) => {
      const ariaLabel =
        button
          .getAttribute("aria-label")
          ?.replace(/\s+/g, " ")
          .trim()
          .toLowerCase() ?? "";

      const text =
        button.textContent?.replace(/\s+/g, " ").trim().toLowerCase() ?? "";

      return (
        ariaLabel.includes("show transcript") ||
        ariaLabel.includes("open transcript") ||
        text.includes("show transcript") ||
        text.includes("open transcript")
      );
    }) ?? null
  );
};

const findTranscriptButton = (): HTMLButtonElement | null =>
  findTranscriptButtonInSection() ?? findTranscriptButtonByText();

const findDescriptionExpandButton = (): HTMLElement | null =>
  document.querySelector<HTMLElement>(
    [
      "#description-inline-expander #expand",
      "ytd-text-inline-expander #expand",
      "#description #expand",
      "tp-yt-paper-button#expand",
      'button[aria-label*="more" i]',
    ].join(","),
  );

const clickElement = (element: HTMLElement): void => {
  element.scrollIntoView({
    block: "center",
    inline: "nearest",
    behavior: "instant",
  });

  element.click();
};

export const openTranscriptPanel = async (): Promise<void> => {
  console.log("[ClaimSift] Attempting to open transcript panel.");

  if (isTranscriptPanelOpen()) {
    console.log("[ClaimSift] Transcript panel is already open.");
    return;
  }

  let transcriptButton = findTranscriptButton();

  console.log("[ClaimSift] Initial transcript button:", transcriptButton);

  if (!transcriptButton) {
    const expandButton = findDescriptionExpandButton();

    console.log("[ClaimSift] Description expand button:", expandButton);

    if (expandButton) {
      clickElement(expandButton);
      await wait(750);
    }

    transcriptButton = findTranscriptButton();

    console.log(
      "[ClaimSift] Transcript button after expanding description:",
      transcriptButton,
    );
  }

  if (!transcriptButton) {
    throw new Error(
      "Show transcript button was not found. The video may not have an available transcript.",
    );
  }

  console.log("[ClaimSift] Clicking transcript button:", {
    text: transcriptButton.textContent?.replace(/\s+/g, " ").trim(),
    ariaLabel: transcriptButton.getAttribute("aria-label"),
    disabled: transcriptButton.disabled,
  });

  clickElement(transcriptButton);

  await wait(750);

  console.log("[ClaimSift] Transcript state after click:", {
    panel: getTranscriptPanel(),
    panelOpen: isTranscriptPanelOpen(),
  });

  if (!isTranscriptPanelOpen()) {
    throw new Error(
      "The transcript button was clicked, but the transcript panel did not open.",
    );
  }
};

const panelHasTranscriptContent = (panel: HTMLElement): boolean => {
  const text = panel.innerText.replace(/\r/g, "").trim();

  if (!text) {
    return false;
  }

  const timestamps = text.match(/(?:\d{1,2}:)?\d{1,2}:\d{2}/g);

  return (timestamps?.length ?? 0) > 0;
};

export const waitForTranscriptContent = (
  timeoutMs = 15_000,
): Promise<HTMLElement> =>
  new Promise((resolve, reject) => {
    let timeoutId: number;

    const findReadyPanel = (): HTMLElement | null => {
      const panel = getTranscriptPanel();

      if (!panel) {
        return null;
      }

      return panelHasTranscriptContent(panel) ? panel : null;
    };

    const existingPanel = findReadyPanel();

    if (existingPanel) {
      resolve(existingPanel);
      return;
    }

    const observer = new MutationObserver(() => {
      const panel = findReadyPanel();

      if (!panel) {
        return;
      }

      observer.disconnect();
      window.clearTimeout(timeoutId);
      resolve(panel);
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      characterData: true,
    });

    timeoutId = window.setTimeout(() => {
      observer.disconnect();

      const panel = getTranscriptPanel();

      console.error("[ClaimSift] Transcript panel diagnostic:", {
        panel,
        panelOpen: isTranscriptPanelOpen(),
        panelText: panel?.innerText.slice(0, 1000),
        panelHtml: panel?.innerHTML.slice(0, 2000),
      });

      reject(
        new Error(`Transcript content did not render within ${timeoutMs}ms.`),
      );
    }, timeoutMs);
  });

const parseTraditionalTranscriptRows = (
  panel: HTMLElement,
): TranscriptSegment[] => {
  const rows = Array.from(
    panel.querySelectorAll<HTMLElement>("ytd-transcript-segment-renderer"),
  );

  console.log("[ClaimSift] Traditional transcript rows:", rows.length);

  return rows
    .map((row): TranscriptSegment | null => {
      const textElement =
        row.querySelector<HTMLElement>(".segment-text") ??
        row.querySelector<HTMLElement>('[class*="segment-text"]');

      const timestampElement =
        row.querySelector<HTMLElement>(".segment-timestamp") ??
        row.querySelector<HTMLElement>('[class*="timestamp"]');

      const text = textElement?.textContent?.replace(/\s+/g, " ").trim();

      const timestamp = timestampElement?.textContent?.trim();

      if (!text || !timestamp) {
        return null;
      }

      return {
        text,
        startSeconds: parseTimestamp(timestamp),
        durationSeconds: 0,
      };
    })
    .filter((segment): segment is TranscriptSegment => segment !== null);
};

const parseTranscriptFromPanelText = (
  panel: HTMLElement,
): TranscriptSegment[] => {
  const lines = panel.innerText
    .replace(/\r/g, "")
    .split("\n")
    .map((line) => line.replace(/\s+/g, " ").trim())
    .filter(Boolean);

  console.log("[ClaimSift] Transcript panel lines:", lines);

  const segments: TranscriptSegment[] = [];

  for (let index = 0; index < lines.length; index += 1) {
    const currentLine = lines[index];

    if (!EXACT_TIMESTAMP_PATTERN.test(currentLine)) {
      continue;
    }

    const timestamp = currentLine;
    const textLines: string[] = [];

    let nextIndex = index + 1;

    if (
      nextIndex < lines.length &&
      DURATION_LABEL_PATTERN.test(lines[nextIndex])
    ) {
      nextIndex += 1;
    }

    while (
      nextIndex < lines.length &&
      !EXACT_TIMESTAMP_PATTERN.test(lines[nextIndex])
    ) {
      const candidate = lines[nextIndex];

      if (
        candidate.toLowerCase() !== "transcript" &&
        candidate.toLowerCase() !== "search transcript"
      ) {
        textLines.push(candidate);
      }

      nextIndex += 1;
    }

    const text = textLines.join(" ").trim();

    if (text) {
      segments.push({
        text,
        startSeconds: parseTimestamp(timestamp),
        durationSeconds: 0,
      });
    }

    index = nextIndex - 1;
  }

  return segments;
};

const removeDuplicateSegments = (
  segments: TranscriptSegment[],
): TranscriptSegment[] => {
  const seen = new Set<string>();

  return segments.filter((segment) => {
    const key = `${segment.startSeconds}:${segment.text}`;

    if (seen.has(key)) {
      return false;
    }

    seen.add(key);
    return true;
  });
};

const addSegmentDurations = (
  segments: TranscriptSegment[],
): TranscriptSegment[] =>
  segments.map((segment, index) => {
    const nextSegment = segments[index + 1];

    if (!nextSegment) {
      return {
        ...segment,
        durationSeconds: 0,
      };
    }

    return {
      ...segment,
      durationSeconds: Math.max(
        0,
        nextSegment.startSeconds - segment.startSeconds,
      ),
    };
  });

export const parseTranscriptPanel = (
  panel: HTMLElement,
): TranscriptSegment[] => {
  const traditionalSegments = parseTraditionalTranscriptRows(panel);

  if (traditionalSegments.length > 0) {
    return addSegmentDurations(removeDuplicateSegments(traditionalSegments));
  }

  const textSegments = parseTranscriptFromPanelText(panel);

  console.log(
    "[ClaimSift] Text-based transcript segments:",
    textSegments.length,
  );

  return addSegmentDurations(removeDuplicateSegments(textSegments));
};

const logTranscriptPanelStructure = (panel: HTMLElement): void => {
  const uniqueTagNames = Array.from(panel.querySelectorAll("*"))
    .map((element) => element.tagName.toLowerCase())
    .filter((tagName, index, tagNames) => tagNames.indexOf(tagName) === index);

  console.log("[ClaimSift] Transcript panel structure:", {
    targetId: panel.getAttribute("target-id"),
    visibility: panel.getAttribute("visibility"),
    uniqueTagNames,
    textPreview: panel.innerText.slice(0, 1000),
    htmlPreview: panel.innerHTML.slice(0, 2000),
  });
};

export const readTranscriptFromPage = async (): Promise<
  TranscriptSegment[]
> => {
  try {
    await openTranscriptPanel();

    const panel = await waitForTranscriptContent();

    logTranscriptPanelStructure(panel);

    const segments = parseTranscriptPanel(panel);

    console.log("[ClaimSift] Parsed transcript segments:", segments.length);

    if (segments.length === 0) {
      console.warn("ClaimSift could not find transcript segments on the page.");
    }

    return segments;
  } catch (error) {
    console.error("[ClaimSift] Transcript unavailable:", error);

    return [];
  }
};
