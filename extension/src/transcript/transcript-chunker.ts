import type { TranscriptChunk, TranscriptSegment } from "../types/transcript";

const MAX_CHUNK_SECONDS = 30;
const MAX_CHUNK_CHARACTERS = 600;

export const chunkTranscript = (
  videoId: string,
  segments: TranscriptSegment[],
): TranscriptChunk[] => {
  const chunks: TranscriptChunk[] = [];

  let currentSegments: TranscriptSegment[] = [];

  const flush = (): void => {
    if (currentSegments.length === 0) {
      return;
    }

    const first = currentSegments[0];
    const last = currentSegments[currentSegments.length - 1];

    chunks.push({
      id: `${videoId}-${first.startSeconds}`,
      videoId,
      text: currentSegments.map((segment) => segment.text).join(" "),
      startSeconds: first.startSeconds,
      endSeconds: last.startSeconds + last.durationSeconds,
    });

    currentSegments = [];
  };

  for (const segment of segments) {
    if (currentSegments.length === 0) {
      currentSegments.push(segment);
      continue;
    }

    const candidateSegments = [...currentSegments, segment];

    const candidateText = candidateSegments.map((item) => item.text).join(" ");

    const first = candidateSegments[0];

    const candidateEnd = segment.startSeconds + segment.durationSeconds;

    const candidateDuration = candidateEnd - first.startSeconds;

    const exceedsTime = candidateDuration > MAX_CHUNK_SECONDS;

    const exceedsCharacters = candidateText.length > MAX_CHUNK_CHARACTERS;

    if (exceedsTime || exceedsCharacters) {
      flush();
    }

    currentSegments.push(segment);
  }

  flush();

  return chunks;
};
