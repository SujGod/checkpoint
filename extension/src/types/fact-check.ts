export type FactCheckVerdict = "TRUE" | "FALSE" | "INCONCLUSIVE";

export interface FactCheck {
  id: string;
  startSeconds: number;
  endSeconds: number;
  claim: string;
  verdict: FactCheckVerdict;
  explanation: string;
  confidence: number;
  sources: FactCheckSource[];
}

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
