import type { ManifestV3Export } from "@crxjs/vite-plugin";

const manifest: ManifestV3Export = {
  manifest_version: 3,
  name: "CheckPoint",
  description: "Overlay timestamped fact checks over YouTube videos.",
  version: "0.1.0",

  permissions: ["storage"],

  host_permissions: ["https://www.youtube.com/*"],

  content_scripts: [
    {
      matches: ["https://www.youtube.com/*"],
      js: ["src/content/content-script.tsx"],
      run_at: "document_idle",
    },
  ],
};

export default manifest;
