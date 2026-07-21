import { createRoot, type Root } from "react-dom/client";
import { Provider } from "react-redux";

import FactCheckOverlayContainer from "../components/FactCheckOverlay/FactCheckOverlayContainer";
import { store } from "../redux/store";
import type { FactCheck } from "../types/fact-check";

let root: Root | null = null;
let container: HTMLDivElement | null = null;

export const createContainer = (): void => {
  document.querySelector("#claimsift-root")?.remove();

  const player =
    document.querySelector<HTMLElement>("#movie_player") ?? document.body;

  container = document.createElement("div");
  container.id = "claimsift-root";

  if (
    player !== document.body &&
    window.getComputedStyle(player).position === "static"
  ) {
    player.style.position = "relative";
  }

  player.appendChild(container);
  root = createRoot(container);
};

export const renderClaimSiftOverlay = (factCheck: FactCheck | null): void => {
  if (!factCheck) {
    root?.render(null);
    return;
  }

  if (!root || !container) {
    createContainer();
  }

  root?.render(
    <Provider store={store}>
      <FactCheckOverlayContainer key={factCheck.id} factCheck={factCheck} />
    </Provider>,
  );
};

export const removeClaimSiftOverlay = (): void => {
  root?.unmount();
  container?.remove();

  root = null;
  container = null;
};
