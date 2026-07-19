export const TRANSCRIPT_PANEL_SELECTORS = [
  'ytd-engagement-panel-section-list-renderer[target-id="engagement-panel-searchable-transcript"]',
  'ytd-engagement-panel-section-list-renderer[target-id="PAmodern_transcript_view"]',
  'ytd-engagement-panel-section-list-renderer[visibility="ENGAGEMENT_PANEL_VISIBILITY_EXPANDED"]',
];

export const TRANSCRIPT_PANEL_SELECTOR = TRANSCRIPT_PANEL_SELECTORS.join(",");
export const EXACT_TIMESTAMP_PATTERN = /^(?:\d{1,2}:)?\d{1,2}:\d{2}$/;
export const TIMESTAMP_PATTERN_SOURCE = /(?:\d{1,2}:)?\d{1,2}:\d{2}/g;

export const MAX_CHUNK_SECONDS = 30;
export const MAX_CHUNK_CHARACTERS = 600;
