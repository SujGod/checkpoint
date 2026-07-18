import { isFactCheckEnabled, setFactCheckEnabled } from "../storage/settings";
import claimSiftIcon from "../assets/claimSiftLogo.png";

const TOGGLE_ID = "check-point-toggle";

interface ToggleCallbacks {
  onEnable: () => void;
  onDisable: () => void;
}

const updateToggleAppearance = (
  button: HTMLButtonElement,
  enabled: boolean,
): void => {
  button.dataset.enabled = String(enabled);
  button.setAttribute("aria-pressed", String(enabled));

  button.title = enabled
    ? "Disable ClaimSift fact checks"
    : "Enable ClaimSift fact checks";
};

const createToggleButton = (): HTMLButtonElement => {
  const button = document.createElement("button");

  button.id = TOGGLE_ID;
  button.type = "button";
  button.className = "ytp-button check-point-toggle";

  button.setAttribute("aria-label", "Toggle ClaimSift fact checks");

  const icon = document.createElement("img");

  icon.src = chrome.runtime.getURL(claimSiftIcon);
  icon.alt = "";
  icon.className = "check-point-toggle-icon";
  icon.setAttribute("aria-hidden", "true");

  button.appendChild(icon);

  return button;
};

export const insertFactCheckToggle = async ({
  onEnable,
  onDisable,
}: ToggleCallbacks): Promise<void> => {
  const controls = document.querySelector<HTMLElement>(".ytp-right-controls");

  if (!controls) {
    return;
  }

  const existingButton = document.querySelector<HTMLButtonElement>(
    `#${TOGGLE_ID}`,
  );

  if (existingButton) {
    return;
  }

  const button = createToggleButton();
  const enabled = await isFactCheckEnabled();

  updateToggleAppearance(button, enabled);

  button.addEventListener("click", async () => {
    const currentValue = await isFactCheckEnabled();
    const nextValue = !currentValue;

    await setFactCheckEnabled(nextValue);
    updateToggleAppearance(button, nextValue);

    if (nextValue) {
      onEnable();
    } else {
      onDisable();
    }
  });

  controls.prepend(button);
};
