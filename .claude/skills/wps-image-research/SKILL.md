---
name: wps-image-research
description: Research and reconstruct diagrams, mind maps, and structured visual content from reference images into editable WPS Mind Map `.pos` files, using native topics, relationships, groups, labels, and styles rather than embedding the source image. Use when the user asks to analyze an image into a WPS mind map, generate or reverse-engineer a `.pos` file, convert a diagram or screenshot into an editable WPS brain map, preserve hierarchy and cross-links, or validate a POS file through WPS import/export.
---

# WPS Image Research

## Core Rule

Treat the reference image as research material, not as the final canvas. Extract its semantic structure and reconstruct that structure as editable WPS mind-map content: topics, parent-child branches, relationships, summaries, labels, notes, and styles. Do not place the whole PNG/JPG/screenshot into the `.pos` file as a substitute for reconstruction.

Treat `.pos` as a WPS Mind Map interchange file, not as a general image format. WPS does not publish a complete stable `.pos` specification, so compatibility must be established from a real file exported by the target WPS version whenever possible.

## When to Use

Use this skill for:

- Turning a flowchart, architecture diagram, research figure, or screenshot into an editable WPS mind map.
- Converting a hierarchical image into a `.pos` file while preserving reading order and nesting.
- Researching a reference image and producing a structured outline before generating a POS file.
- Modifying an existing `.pos` file's topics, hierarchy, labels, colors, or layout.
- Reverse-engineering a POS sample to determine its package/container and data model.
- Validating that a generated or modified POS file imports into WPS and remains editable.

Do not use it for:

- Delivering a raster image with a `.pos` extension.
- Pixel-perfect reproduction of charts, photographs, or dense illustrations where mind-map semantics do not fit.
- Assuming that a JSON or ZIP structure described in a patent is automatically compatible with every WPS version.
- Editing a POS file by changing its extension without inspecting its contents.

## Input Research Workflow

1. Inspect the inputs.
   - Confirm the reference image path, any existing `.pos` sample, the target WPS version, requested output path, and whether PNG/PDF previews are also needed.
   - Preserve the original sample and create a working copy before reverse engineering.
   - Record image dimensions, orientation, whitespace, and likely reading order.

2. Build a visual inventory.
   - Identify the central subject or root topic.
   - List first-level branches, nested topics, sibling order, and terminal nodes.
   - Record cross-links, feedback arrows, groups, braces, summaries, legends, captions, and notes separately from ordinary hierarchy.
   - Preserve legible text exactly; mark uncertain or unreadable text instead of silently inventing it.
   - Record visual tokens: theme, branch colors, topic shapes, border styles, typography, spacing, and emphasis.

3. Map the image to mind-map semantics.
   - Use parent-child topics for hierarchy and decomposition.
   - Use relationship links for non-hierarchical arrows or dependencies.
   - Use summaries or grouped branches for enclosing regions when supported by the target WPS version.
   - Use notes or labels for captions and explanatory text that should not become branch topics.
   - Use a plain outline fallback for content that cannot be represented natively, and report the approximation.

4. Establish a coordinate and layout plan.
   - Decide whether the reference is radial, tree, left-to-right, right-to-left, timeline-like, or a diagram that needs a nearest mind-map representation.
   - Calibrate the canvas and major regions first.
   - Keep sibling order deterministic and assign stable IDs before writing the file.
   - Avoid manually encoding absolute positions until a real POS sample proves the relevant fields and coordinate system.

## `.pos` Compatibility Workflow

Because the internal format is not publicly specified, use sample-driven compatibility rather than guessing.

1. Obtain a minimal `.pos` exported by the exact WPS product/version when available. Prefer samples containing one root, one child, and one relationship only when investigating links.
2. Inspect the file signature and container before parsing. Check whether it is a ZIP package, plain text, JSON, XML, or an opaque binary file. Never assume the extension identifies the container.
3. If it is a ZIP package, inspect the entry names, compression method, manifest, media directory, metadata, and encoding. Preserve package paths and required metadata entries.
4. Compare two samples that differ in exactly one way: root text, one child, one style, or one relationship. Use binary and decompressed diffs to identify stable fields, generated IDs, checksums, and version markers.
5. Infer only fields proven by the samples. Keep unknown fields and package members unchanged when modifying an existing file.
6. Generate a minimal file first. Add hierarchy, styling, relationships, summaries, and notes incrementally, validating after each capability.
7. Import the result through WPS's Mind Map import flow. A file that merely opens as a ZIP or parses as JSON is not proven compatible.
8. Re-export the imported file from WPS and compare the round trip. Confirm topics remain editable and that no content silently disappears.

Useful local inspection commands, when available:

```powershell
Format-Hex -Path .\sample.pos -Count 32
Get-Command 7z, unzip -ErrorAction SilentlyContinue
7z l .\sample.pos
```

For a ZIP-backed sample:

```powershell
Copy-Item .\sample.pos .\sample.zip
Expand-Archive .\sample.zip .\sample-unpacked
```

Do not commit extracted proprietary sample contents or user documents unless explicitly requested.

## Generation Strategy

For a new POS file:

- Prefer a small deterministic generator that writes only a format proven by a target-version sample.
- Use stable topic IDs and explicit parent IDs.
- Preserve source text as Unicode and verify encoding after serialization.
- Keep branch order stable so repeated runs produce reviewable output.
- Separate content extraction from serialization: first produce a validated topic tree and relationship inventory, then encode it into POS.
- Do not add unsupported fields merely because another mind-map format uses them.
- If no real sample is available, generate an intermediate structured outline and state that a WPS-exported template is required before claiming a compatible `.pos` deliverable.

For an existing POS file:

- Make a backup before modification.
- Prefer changing known topic text or style fields in a copy of the package.
- Preserve unrelated package members, document metadata, IDs, ordering, and checksums unless the format requires regeneration.
- Avoid global string replacement: IDs and style values may be shared by unrelated topics.
- Reopen and round-trip through WPS after changes.

## Verification Checklist

A result is acceptable only when the evidence matches the requested scope:

- The `.pos` file was generated from a real WPS-compatible template or its compatibility limitation is explicitly stated.
- WPS imports the file through the Mind Map import flow without an error.
- The root topic, branch hierarchy, sibling order, important labels, and relationships are present.
- Topics and labels remain individually editable after import.
- The reference image is not embedded as a full-page raster substitute.
- Chinese and other non-ASCII text renders correctly.
- Branches do not overlap severely or lose their intended reading order.
- A WPS round-trip export preserves the reconstructed content.
- Any requested preview is exported from the imported POS document or is clearly labeled as a separate approximation.
- Original input files and proprietary sample files were not overwritten.

If WPS is unavailable, perform static checks only and say so. Static checks can verify container integrity, required entries, text presence, and non-zero file size; they cannot prove WPS compatibility.

## Output Contract

The final response should include:

- Generated `.pos` path.
- Source sample/template path used for compatibility, if any.
- Preview paths, if requested.
- A concise summary of the extracted root, branches, and relationships.
- Whether the result was imported and round-tripped in WPS.
- Whether content is native editable WPS mind-map data rather than an embedded image.
- Any unreadable labels, approximations, unsupported diagram features, or missing WPS validation.

## Safety and Scope

- Do not overwrite reference images, source POS files, or user documents.
- Do not publish or upload proprietary reference images or POS samples to third-party converters.
- Treat embedded media and notes as potentially sensitive user content.
- Do not claim that a guessed binary/package layout is a valid WPS POS file.
- If the image is a chart or data visualization, preserve the underlying data separately when available; a mind map is not a substitute for a quantitative chart.
