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
    });
  }, [claim, checkClaim]);

  if (isLoading) {
    console.log("ClaimSift request loading");
    return null;
  }

  if (isError) {
    console.error("ClaimSift request failed:", error);
    return null;
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
