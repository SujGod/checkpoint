export type FactCheckVerdict = "TRUE" | "FALSE" | "INCONCLUSIVE";
export type FactCheckStatus = "PENDING" | "CHECKING" | "COMPLETE" | "FAILED";

export interface VideoFactCheckManifest {
  videoId: string;
  factChecks: FactCheck[];
}

export type FactCheck = {
  id: string;
  claim: string;
  startSeconds: number;
  endSeconds: number;
};

export interface FactCheckRequest {
  claim: string;
}

export interface FactCheckSource {
  title: string;
  publisher: string;
  url: string;
}

export interface FactCheckResponse {
  claim: string;
  verdict: FactCheckVerdict;
  explanation: string;
  sources: FactCheckSource[];
}
