import type { FactCheck } from "../../types/fact-check";
import FactCheckOverlay from "./FactCheckOverlay";

interface FactCheckOverlayContainerProps {
  factCheck: FactCheck;
}

const FactCheckOverlayContainer = ({
  factCheck,
}: FactCheckOverlayContainerProps) => {
  const firstSource = factCheck.sources?.[0];

  const handleViewSources = firstSource?.url
    ? () => {
        window.open(firstSource.url, "_blank", "noopener,noreferrer");
      }
    : undefined;

  return (
    <FactCheckOverlay
      claim={factCheck.claim}
      verdict={factCheck.verdict}
      explanation={factCheck.explanation}
      onViewSources={handleViewSources}
    />
  );
};

export default FactCheckOverlayContainer;
