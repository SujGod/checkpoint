export interface TranscriptSegment {
  text: string;
  startSeconds: number;
  durationSeconds: number;
}

export interface TranscriptChunk {
  id: string;
  videoId: string;
  text: string;
  startSeconds: number;
  endSeconds: number;
}

export type ExtractedClaim = {
  text: string;
  startSeconds: number;
  endSeconds: number;
};

export type ExtractClaimsRequest = {
  videoId: string;
  chunkId: string;
  text: string;
  startSeconds: number;
  endSeconds: number;
};

export type ExtractClaimsResponse = {
  videoId: string;
  chunkId: string;
  claims: ExtractedClaim[];
};
