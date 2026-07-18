import type { FactCheck } from "../types/fact-check";
import type { ExtractedClaim } from "../types/transcript";

const normalizeClaimText = (text: string | null | undefined): string =>
  (text ?? "")
    .toLowerCase()
    .replace(/[^\p{L}\p{N}\s]/gu, "")
    .replace(/\s+/g, " ")
    .trim();

const isValidExtractedClaim = (
  claim: ExtractedClaim | null | undefined,
): claim is ExtractedClaim => {
  if (!claim) {
    return false;
  }

  if (typeof claim.text !== "string" || claim.text.trim().length === 0) {
    console.warn("[ClaimSift] Ignoring claim with invalid text:", claim);

    return false;
  }

  if (
    typeof claim.startSeconds !== "number" ||
    !Number.isFinite(claim.startSeconds)
  ) {
    console.warn(
      "[ClaimSift] Ignoring claim with invalid startSeconds:",
      claim,
    );

    return false;
  }

  if (
    typeof claim.endSeconds !== "number" ||
    !Number.isFinite(claim.endSeconds)
  ) {
    console.warn("[ClaimSift] Ignoring claim with invalid endSeconds:", claim);

    return false;
  }

  return true;
};

export const deduplicateClaims = (
  claims: Array<ExtractedClaim | null | undefined>,
): ExtractedClaim[] => {
  const seen = new Set<string>();

  return claims.filter(isValidExtractedClaim).filter((claim) => {
    const normalizedText = normalizeClaimText(claim.text);

    if (!normalizedText) {
      return false;
    }

    const key = `${Math.floor(claim.startSeconds)}:${normalizedText}`;

    if (seen.has(key)) {
      return false;
    }

    seen.add(key);
    return true;
  });
};

export const normalizeClaimTiming = (claim: ExtractedClaim): ExtractedClaim => {
  const startSeconds = Math.max(0, claim.startSeconds);

  const endSeconds =
    claim.endSeconds > startSeconds ? claim.endSeconds : startSeconds + 8;

  return {
    ...claim,
    startSeconds,
    endSeconds,
  };
};

const MINIMUM_DISPLAY_SECONDS = 15;

export const buildVideoManifest = (claims: ExtractedClaim[]): FactCheck[] =>
  claims
    .map((claim, index): FactCheck => {
      const startSeconds = Math.max(0, claim.startSeconds);

      const spokenEndSeconds = Math.max(claim.endSeconds, startSeconds);

      return {
        id: `claim-${startSeconds}-${index}`,
        claim: claim.text,
        startSeconds,
        endSeconds: Math.max(
          spokenEndSeconds,
          startSeconds + MINIMUM_DISPLAY_SECONDS,
        ),
      };
    })
    .sort((a, b) => a.startSeconds - b.startSeconds);
