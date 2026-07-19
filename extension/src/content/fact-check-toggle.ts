import { isFactCheckEnabled, setFactCheckEnabled } from "../storage/settings";
import claimSiftIcon from "../assets/claimSiftLogo.png";

const TOGGLE_ID = "claimsift-toggle";
const PLAYER_CONTROLS_SELECTOR = ".ytp-right-controls";
const CONTROLS_WAIT_TIMEOUT_MS = 10_000;

interface ToggleCallbacks {
  onEnable: () => void | Promise<void>;
  onDisable: () => void | Promise<void>;
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
  button.className = "ytp-button claimsift-toggle";

  button.setAttribute("aria-label", "Toggle ClaimSift fact checks");

  const icon = document.createElement("img");

  icon.src = chrome.runtime.getURL(claimSiftIcon);
  icon.alt = "";
  icon.className = "claimsift-toggle-icon";
  icon.setAttribute("aria-hidden", "true");

  button.appendChild(icon);

  return button;
};

const waitForPlayerControls = (
  timeoutMs = CONTROLS_WAIT_TIMEOUT_MS,
): Promise<HTMLElement> =>
  new Promise((resolve, reject) => {
    const existingControls = document.querySelector<HTMLElement>(
      PLAYER_CONTROLS_SELECTOR,
    );

    if (existingControls) {
      resolve(existingControls);
      return;
    }

    const observer = new MutationObserver(() => {
      const controls = document.querySelector<HTMLElement>(
        PLAYER_CONTROLS_SELECTOR,
      );

      if (!controls) {
        return;
      }

      observer.disconnect();
      window.clearTimeout(timeoutId);
      resolve(controls);
    });

    observer.observe(document.documentElement, {
      childList: true,
      subtree: true,
    });

    const timeoutId = window.setTimeout(() => {
      observer.disconnect();

      reject(new Error("YouTube player controls were not found."));
    }, timeoutMs);
  });

const removeDetachedToggle = (): void => {
  const existingButton = document.querySelector<HTMLButtonElement>(
    `#${TOGGLE_ID}`,
  );

  if (existingButton && !existingButton.isConnected) {
    existingButton.remove();
  }
};

export const insertFactCheckToggle = async ({
  onEnable,
  onDisable,
}: ToggleCallbacks): Promise<void> => {
  try {
    removeDetachedToggle();

    const controls = await waitForPlayerControls();

    const existingButton = document.querySelector<HTMLButtonElement>(
      `#${TOGGLE_ID}`,
    );

    if (existingButton && existingButton.parentElement === controls) {
      const enabled = await isFactCheckEnabled();

      updateToggleAppearance(existingButton, enabled);

      return;
    }

    /*
     * YouTube may replace the player control container during
     * navigation. Remove a stale button before inserting the
     * current one into the active controls.
     */
    existingButton?.remove();

    const button = createToggleButton();

    const enabled = await isFactCheckEnabled();

    updateToggleAppearance(button, enabled);

    button.addEventListener("click", async () => {
      /*
       * Prevent rapid double-clicks from triggering overlapping
       * storage updates and duplicate processing.
       */
      if (button.dataset.processing === "true") {
        return;
      }

      button.dataset.processing = "true";
      button.disabled = true;

      try {
        const currentValue = await isFactCheckEnabled();

        const nextValue = !currentValue;

        await setFactCheckEnabled(nextValue);

        updateToggleAppearance(button, nextValue);

        if (nextValue) {
          await onEnable();
        } else {
          await onDisable();
        }
      } catch (error) {
        console.error("[ClaimSift] Failed to update toggle:", error);
      } finally {
        button.disabled = false;
        delete button.dataset.processing;
      }
    });

    controls.prepend(button);

    console.log("[ClaimSift] Toggle inserted.");
  } catch (error) {
    console.error("[ClaimSift] Could not insert toggle:", error);
  }
};
