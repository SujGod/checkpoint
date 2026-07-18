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
    <aside className="claimsift-overlay" aria-label="Fact-check information">
      <div className="claimsift-content">
        <div
          className={`claimsift-verdict claimsift-verdict--${verdict.toLowerCase()}`}
        >
          {verdict}
          <img
            src={verdictIcons[verdict]}
            alt=""
            className="claimsift-verdict-icon"
            aria-hidden="true"
          />
        </div>
        <div className="claimsift-claim">“{claim}”</div>
        <div className="claimsift-explanation">{explanation}</div>
        {onViewSources && (
          <button
            type="button"
            className="claimsift-source-button"
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
