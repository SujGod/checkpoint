export type FactCheckVerdict = "TRUE" | "FALSE" | "INCONCLUSIVE";
export type FactCheckStatus = "PENDING" | "CHECKING" | "COMPLETE" | "FAILED";

export interface VideoFactCheckManifest {
  videoId: string;
  factChecks: FactCheck[];
}

export type FactCheckSource = {
  title?: string;
  url?: string;
  publisher?: string;
};

export type FactCheck = {
  id: string;
  claim: string;
  startSeconds: number;
  endSeconds: number;
  verdict: FactCheckVerdict;
  explanation: string | null;
  sources: FactCheckSource[];
};

export interface FactCheckRequest {
  claim: string;
}

export interface FactCheckResponse {
  claim: string;
  verdict: FactCheckVerdict;
  explanation: string;
  sources: FactCheckSource[];
}

export type ProcessVideoResponse = {
  videoId: string;
  factChecks: FactCheck[];
};
