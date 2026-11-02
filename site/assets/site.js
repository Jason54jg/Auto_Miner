"use strict";
const releases = JSON.parse(
  document.getElementById("release-data").textContent,
);
const selector = document.getElementById("minecraft-version");
function updateVersion() {
  const version = releases.minecraft.find(
    (entry) => entry.version === selector.value,
  );
  if (!version) return;
  document.getElementById("fabric-version").textContent = version.fabric_api;
  document.getElementById("kotlin-version").textContent = version.fabric_kotlin;
  document.getElementById("modmenu-version").textContent = version.modmenu;
  document.getElementById("version-download").href =
    `${releases.modrinth_url}/versions?g=${encodeURIComponent(version.version)}&l=fabric`;
}
selector.addEventListener("change", updateVersion);
window.addEventListener("pageshow", updateVersion);
updateVersion();
const copyButton = document.getElementById("copy-command");
copyButton.addEventListener("click", async () => {
  const status = document.getElementById("copy-status");
  try {
    await navigator.clipboard.writeText("/autominer");
    status.textContent = copyButton.dataset.copied;
  } catch {
    status.textContent = copyButton.dataset.failed;
  }
});
