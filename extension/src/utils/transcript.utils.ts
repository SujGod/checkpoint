import type { TranscriptSegment } from "../types/transcript";

export const parseTimestamp = (timestamp: string): number => {
  const parts = timestamp.split(":").map(Number);

  if (parts.some((part) => Number.isNaN(part))) {
    throw new Error(`Invalid transcript timestamp: ${timestamp}`);
  }

  if (parts.length === 2) {
    const [minutes, seconds] = parts;

    return minutes * 60 + seconds;
  }

  if (parts.length === 3) {
    const [hours, minutes, seconds] = parts;

    return hours * 3600 + minutes * 60 + seconds;
  }

  throw new Error(`Unsupported transcript timestamp: ${timestamp}`);
};

export const calculateTranscriptDurations = (
  segments: TranscriptSegment[],
): TranscriptSegment[] => {
  return segments.map((segment, index) => {
    const nextSegment = segments[index + 1];

    const durationSeconds = nextSegment
      ? Math.max(0, nextSegment.startSeconds - segment.startSeconds)
      : 4;

    return {
      ...segment,
      durationSeconds,
    };
  });
};
