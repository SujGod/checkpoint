import "./FactCheckOverlay.css";
import trueIcon from "../../assets/green_checkmark.png";
import falseIcon from "../../assets/red_x.png";
import inconclusiveIcon from "../../assets/yellow_caution_sign.png";
import type { FactCheckVerdict } from "../../types/fact-check";

interface FactCheckOverlayProps {
  claim: string;
  verdict: FactCheckVerdict;
  explanation: string;
  onViewSources?: () => void;
}

const verdictIcons: Record<FactCheckVerdict, string> = {
  TRUE: chrome.runtime.getURL(trueIcon),
  FALSE: chrome.runtime.getURL(falseIcon),
  INCONCLUSIVE: chrome.runtime.getURL(inconclusiveIcon),
};

const FactCheckOverlay = ({
  claim,
  verdict,
  explanation,
  onViewSources,
}: FactCheckOverlayProps) => {
  return (
    <aside className="check-point-overlay" aria-label="Fact-check information">
      <div className="check-point-content">
        <div
          className={`check-point-verdict check-point-verdict--${verdict.toLowerCase()}`}
        >
          {verdict}
          <img
            src={verdictIcons[verdict]}
            alt=""
            className="check-point-verdict-icon"
            aria-hidden="true"
          />
        </div>
        <div className="check-point-claim">“{claim}”</div>
        <div className="check-point-explanation">{explanation}</div>
        {onViewSources && (
          <button
            type="button"
            className="check-point-source-button"
            onClick={onViewSources}
          >
            View sources
          </button>
        )}
      </div>
    </aside>
  );
};

export default FactCheckOverlay;
