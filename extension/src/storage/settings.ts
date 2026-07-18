const FACT_CHECK_ENABLED_KEY = "factCheckEnabled";

export async function isFactCheckEnabled(): Promise<boolean> {
  const result = await chrome.storage.local.get(
    FACT_CHECK_ENABLED_KEY
  );

  return result[FACT_CHECK_ENABLED_KEY] === true;
}

export async function setFactCheckEnabled(
  enabled: boolean
): Promise<void> {
  await chrome.storage.local.set({
    [FACT_CHECK_ENABLED_KEY]: enabled
  });
}