import { useEffect, useRef } from "react";

import { useCheckClaimMutation } from "../../api/claimsiftApi";
import FactCheckOverlay from "./FactCheckOverlay";

interface FactCheckOverlayContainerProps {
  claim: string;
}

const FactCheckOverlayContainer = ({
  claim,
}: FactCheckOverlayContainerProps) => {
  const submittedClaimRef = useRef<string | null>(null);

  const [checkClaim, { data, isLoading, isError, error }] =
    useCheckClaimMutation();

  useEffect(() => {
    const normalizedClaim = claim.trim();

    if (!normalizedClaim) {
      return;
    }

    if (submittedClaimRef.current === normalizedClaim) {
      return;
    }

    submittedClaimRef.current = normalizedClaim;

    console.count("ClaimSift POST request");

    void checkClaim({
      claim: normalizedClaim,
    })
      .unwrap()
      .then((response) => {
        console.log("[ClaimSift] Fact-check response:", response);
      })
      .catch((mutationError: unknown) => {
        console.error("[ClaimSift] Fact-check mutation failed:", mutationError);
      });
  }, [claim, checkClaim]);

  if (isLoading) {
    return (
      <aside className="claimsift-overlay" aria-label="Fact-check information">
        <div className="fact-check-overlay">
          <div className="fact-check-overlay__claim"></div>
          <div className="fact-check-overlay__status">Checking…</div>
        </div>
      </aside>
    );
  }

  if (isError) {
    console.error("ClaimSift request failed:", error);
    return (
      <aside className="claimsift-overlay" aria-label="Fact-check information">
        <div className="fact-check-overlay">
          <div className="fact-check-overlay__claim"></div>
          <div className="fact-check-overlay__status">Unable to Check</div>
        </div>
      </aside>
    );
  }

  if (!data) {
    return null;
  }

  const firstSource = data.sources?.[0];

  const handleViewSources = firstSource
    ? () => {
        window.open(firstSource.url, "_blank", "noopener,noreferrer");
      }
    : undefined;

  return (
    <FactCheckOverlay
      claim={data.claim}
      verdict={data.verdict}
      explanation={data.explanation}
      onViewSources={handleViewSources}
    />
  );
};

export default FactCheckOverlayContainer;
