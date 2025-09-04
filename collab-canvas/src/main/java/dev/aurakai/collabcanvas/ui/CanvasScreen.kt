package dev.aurakai.collabcanvas.ui

/**
 * Displays a collaborative drawing canvas with multi-tool support, gesture handling, and animated path rendering.
 */
/**
 * An interactive composable screen that provides a collaborative drawing canvas with multi-tool support.
 *
 * Supports freehand path drawing, basic rectangle and oval elements, pinch-to-zoom, panning, and progressive
 * animated rendering of previously drawn paths. The UI includes a top app bar (clear and save actions),
 * floating tool buttons to select Path/Rectangle/Oval, and a toolbar for color, stroke width selection, and clearing.
 *
 * State managed by this composable:
 * - A list of completed drawable paths (with animated copies for progressive rendering).
 * - A list of canvas elements (path/rectangle/oval).
 * - Current in-progress Path, current color, stroke width, selected tool/element, and an isDrawing flag.
 * - Animatables for zoom (scale) and pan (offset), and a transformable gesture state for handling pinch/drag gestures.
 *
 * Side effects and notable behavior:
 * - Mutates internal state lists (paths, elements, animatedPaths) as the user draws or clears the canvas.
 * - Pinch-to-zoom and pan update the internal scale and offset animatables.
 * - The top-bar "Clear Canvas" and toolbar "Clear" actions remove stored paths and animated paths.
 * - The "Save" action is a placeholder and does not persist the canvas in this implementation.
 */

/**
 * Renders the collaborative drawing canvas screen with tools, gestures, and animated rendering.
 *
 * Provides a full-screen drawing surface that supports:
 * - Freehand drawing, rectangle and oval tools selectable via floating action buttons.
 * - Pinch-to-zoom and pan (transformable) interactions.
 * - Tap and drag gesture handling for creating and committing drawable paths.
 * - Animated progressive rendering of previously committed paths.
 * - A top app bar with actions to clear the canvas and save (placeholder).
 * - An on-screen toolbar for selecting color, stroke width, and clearing animated paths.
 *
 * The composable owns and remembers all canvas state (paths, elements, current in-progress path,
 * selected tool/color/stroke, animation state, scale and offset). Committing a completed path
 * adds a PluckablePath to the internal lists and creates an animated copy for playback.
 */
/**
 * Full-screen composable providing an interactive collaborative drawing canvas.
 *
 * Supports freehand drawing (PATH) plus rectangle and oval tools, pinch-to-zoom and pan, tap/drag gestures
 * to create strokes, and animated playback of committed paths. The UI includes a top app bar with clear
 * and save actions (save is a placeholder), floating tool buttons to switch between PATH/RECTANGLE/OVAL,
 * and an on-screen toolbar for selecting color, stroke width, and clearing animated paths.
 *
 * Internal state owned by this composable includes the list of committed PluckablePath objects (`paths`),
 * a list of drawable CanvasElement objects (`elements`), the in-progress `currentPath`, current color and
 * stroke width, the selected tool and element, animation state for progressive rendering (`animatedPaths`),
 * and transform state for zoom (`scale`) and pan (`offset`). Committed paths are added to `paths` when a
 * drawing gesture ends and the current path has non-empty bounds. Clearing actions remove paths, elements,
 * and their animated copies.
 */
/**
 * Renders the interactive collaborative drawing surface for creating and animating simple canvas elements.
 *
 * This full-screen composable provides:
 * - Multiple drawing tools: freehand PATH, RECTANGLE, and OVAL.
 * - Pinch-to-zoom and pan support via a transformable state.
 * - Tap and drag input for creating in-progress strokes (freehand PATH); completed strokes are committed when the gesture ends and the path has non-empty bounds.
 * - A top app bar with Clear and Save actions and floating tool buttons to switch tools.
 * - A toolbar for selecting color and stroke width and clearing committed/animated paths.
 * - Animated playback support: committed paths are stored in `paths` and mirrored into `animatedPaths` for progressive rendering.
 *
 * Visual content (grid, elements, current in-progress path, and animated paths) is drawn under the current scale and offset transforms so zoom and pan affect all canvas content consistently.


/**
 * Full-screen interactive collaborative drawing canvas.
 *
 * Supports three tools (freehand PATH, RECTANGLE, OVAL), pinch-to-zoom and pan, tap/drag gestures to create strokes,
 * and animated playback of committed paths. The UI includes a top app bar with clear and save actions (save is a placeholder),
 * floating tool buttons to switch tools, and an on-screen toolbar for color, stroke width, and clearing animated paths.
 *
 * State managed internally includes committed paths (with separate animated copies), transient drawing state (current path,
 * color, stroke width, selected tool), zoom/offset animations, and a list of other canvas elements. Committed paths are mirrored
 * into an animatedPaths map for progressive rendering; clearing actions remove entries from these collections.
=======
 * Composable screen that provides an interactive collaborative drawing canvas.
 *
 * Features: freehand drawing, rectangle and oval tools, pan & pinch-zoom gestures, animated replay of
 * completed paths, a grid background, tool FABs, a top app bar with clear/save actions, and an overlaid
 * toolbar for color/stroke selection and clearing. The composable maintains internal mutable state for:
 * - completed pluckable paths and a map of animated copies,
 * - drawable elements (paths, rectangles, ovals),
 * - the in-progress Path, current color, stroke width, selected tool, and drawing flag,
 * - scale and pan offsets as Animatable values.
 *
 * Side effects:
 * - Adds completed paths to the internal `paths` list when a drag ends (if non-empty).
 * - Creates animated copies for new paths via a LaunchedEffect watching `paths`.
 * - Clearing actions (top-bar delete and toolbar clear) clear `paths`, `elements` (top-bar), and `animatedPaths`.
 *
 * This is a UI entry point and does not take parameters or return a value.

 * A composable full-screen collaborative drawing canvas with multi-tool support, pan/zoom gestures, and animated replay of completed strokes.
 *
 * This function hosts the UI and state for an interactive drawing surface:
 * - Maintains in-progress drawing state (current path, color, stroke width, selected tool).
 * - Stores completed drawable items in `paths` and `elements`, and keeps per-path animated copies in `animatedPaths` for progressive rendering.
 * - Supports three tools (path, rectangle, oval) selectable via floating action buttons; freehand drawing is performed when the PATH tool is active.
 * - Handles pinch-to-zoom and pan via a transformable state (`scale` and `offset`) and adjusts pointer interactions so strokes and toolbar behave consistently under zoom.
 * - Detects tap/drag gestures to begin, update, and finish drawings; completed paths are added to `paths`.
 * - Renders a background grid, persisted elements, the current in-progress path (with stroke scaled to remain visually consistent while zoomed), and animated playback of previous paths.
 * - Exposes UI actions to clear the canvas (removes `paths`, `elements`, and `animatedPaths`) and a placeholder save action.
 *
 * This composable does not return a value; it manages its own internal Compose state and side effects and composes child UI such as `CanvasToolbar`.
 */
/**
 * Full-screen, interactive collaborative drawing canvas composable.
 *
 * Provides a pinch-to-zoom and pan-enabled drawing surface with multiple tools (freehand/path,
 * rectangle, oval), color and stroke-width selection, animated replay of completed strokes,
 * and controls to clear or save the canvas. Maintains local UI state for:
 * - completed drawable items (paths and persisted elements),
 * - the in-progress Path, selected tool, color, stroke width, and drawing flag,
 * - animated copies used for progressive replay,
 * - zoom scale and pan offset as Animatable states.
 *
 * User interactions:
 * - Tap/drag gestures draw a freehand path when the PATH tool is selected.
 * - Drag gestures start, extend, and finish the current path; finished non-empty paths are
 *   appended to the internal `paths` list and also copied into `animatedPaths` for replay.
 * - Floating action buttons switch tools; the toolbar controls color, stroke width, and clearing.
 *
 * Rendering:
 * - Draws a background grid, persisted canvas elements (paths, rectangles, ovals), the current
 *   in-progress path (scaled to remain visually consistent under zoom), and animated paths
 *   (each with its own scale, offset, and alpha).
 *
 * Side effects:
 * - Mutates local remembered state lists/maps: `paths`, `elements`, and `animatedPaths`.
 * - Updates `scale` and `offset` Animatable states in response to transform gestures.
 *
 * Note: This composable is UI/stateful and intended to be used directly within a Compose hierarchy;
 * it does not persist canvas contents outside its in-memory state.
 */
/**
 * Full-screen collaborative drawing surface with pan/zoom, multiple tools, and animated stroke replay.
 *
 * Provides an in-memory canvas UI that supports freehand drawing (path), rectangles, and ovals;
 * pan/zoom gestures; a translucent grid background; tool selection FABs; and a toolbar for color,
 * stroke width, and clearing the canvas. In-progress strokes are rendered while drawing and completed
 * strokes are stored in `paths` (with per-stroke animation metadata kept in `animatedPaths`) for
 * replay/animation. Global transforms (scale and offset) are applied to all drawing operations so
 * content remains consistent under zoom and pan.
 *
 * Side effects:
 * - Mutates internal Compose state (in-memory lists `paths`, `elements`, and `animatedPaths`,
 *   plus transient state like `currentPath`, `currentColor`, `strokeWidth`, `selectedTool`,
 *   `isDrawing`, `scale`, and `offset`).
 * - Clearing the canvas or the Delete action removes only in-memory state; there is no persistence.
 * - The Save action is present in the UI but is a placeholder (no save implementation).
 */
/**
 * Full-screen interactive drawing canvas composable with multi-tool support, pan/zoom, and animated playback.
 *
 * Provides an in-memory drawing surface where the user can draw freeform paths, create rectangles and ovals,
 * pan and zoom the view, and see an animated replay of completed paths. Maintains internal state for:
 * - persisted drawable paths and elements,
 * - the in-progress path, color, stroke width, selected tool, and drawing flag,
 * - per-path animation state (scale, offset, alpha),
 * - global scale and pan offset for the canvas.
 *
 * Interaction details:
 * - PATH tool: freehand drawing via drag gestures (start -> lineTo -> commit on drag end). Taps begin a path.
 * - RECTANGLE / OVAL tools: reserved in the UI; element creation derives bounds from a Path (no explicit shape-drag behaviour implemented here).
 * - Pinch-to-zoom and pan gestures update the canvas transform; panning is adjusted by the current scale to remain intuitive.
 *
 * Side effects:
 * - Clearing actions (toolbar Clear or top-bar Clear) remove in-memory paths, elements, and animated state only.
 * - The top-bar "Save" action is a placeholder and does not persist data.
 *
 * Visual notes:
 * - The in-progress path stroke is scaled to remain visually consistent under zoom.
 * - A grid background is drawn under all content.
 */
@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)

/**
 * Full-screen composable providing an interactive collaborative drawing canvas.
 *
 * Renders a pan- and zoom-able canvas with a background grid, selectable drawing tools
 * (freehand PATH, RECTANGLE, OVAL), a live in-progress stroke, and playback-ready committed paths.
 * Users can draw freehand with drag/tap gestures (PATH), pan/zoom via transform gestures, select
 * tool, color, and stroke width via the toolbar, and clear or save the canvas from the top app bar.
 *
 * Visual content (grid, elements, current stroke, and animated/committed paths) is uniformly
 * transformed by the current scale and offset so zooming and panning affect all layers together.
 * Committed paths are stored in `paths` and mirrored into `animatedPaths` for animated playback.
 *
 * Side effects:
 * - Maintains and mutates internal remembered state: committed `paths`, `elements`, `animatedPaths`,
 *   current in-progress `currentPath`, `currentColor`, `strokeWidth`, selected tool, and transform
 *   state (`scale`, `offset`).
 * - Clearing actions remove entries from `paths`, `elements`, and `animatedPaths`.
*/
@Composable
fun CanvasScreen() {
    // Canvas state
    val paths = remember { mutableStateListOf<PluckablePath>() }
    val elements = remember { mutableStateListOf<CanvasElement>() }
    var currentPath by remember { mutableStateOf(Path()) }
    var currentColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableStateOf(5f) }
    var selectedTool by remember { mutableStateOf<ElementType>(ElementType.PATH) }
    var isDrawing by remember { mutableStateOf(false) }
// Canvas state
val paths = remember { mutableStateListOf<PluckablePath>() }
val elements = remember { mutableStateListOf<CanvasElement>() }
var currentPath by remember { mutableStateOf(Path()) }
var currentColor by remember { mutableStateOf(Color.Black) }
var strokeWidth by remember { mutableStateOf(5f) }
var selectedTool by remember { mutableStateOf<ElementType>(ElementType.PATH) }
var selectedElement by remember { mutableStateOf<CanvasElement?>(null) }
var isDrawing by remember { mutableStateOf(false) }

val coroutineScope = rememberCoroutineScope()
rememberScrollState()

// Animation states
val animatedPaths = remember { mutableStateMapOf<Int, PluckablePath>() }
val scale = remember { Animatable(1f) }
val offset = remember { Animatable(Offset.Zero, Offset.VectorConverter) }

// Update animated paths when paths change
for (i in paths.indices) {
val path = path.copy()
if (!animatedPaths.containsKey(i)) {
animatedPaths = path.copy()
}
}

// Canvas gesture handlers
val panZoomState = rememberTransformableState { zoomChange, panChange, rotationChange ->
coroutineScope.launch {
scale.snapTo(scale.value * zoomChange)
val newOffset = offset.value + panChange * (1f / scale.value)
offset.snapTo(newOffset)
}
}

Scaffold(
topBar = {
TopAppBar(
title = { Text("Co-lab Canvas") },
actions = {
IconButton(onClick = {
paths.clear()
elements.clear()
animatedPaths.clear()
}) {
Icon(Icons.Default.Delete, "Clear Canvas")
}
IconButton(onClick = { /* Save canvas */ }) {
Icon(Icons.Default.Check, "Save")
}
}
)
},
floatingActionButton = {
Column(
horizontalAlignment = Alignment.End,
verticalArrangement = Arrangement.spacedBy(8.dp)
) {
// Tool selection buttons
FloatingActionButton(
onClick = { selectedTool = ElementType.PATH },
modifier = Modifier.size(48.dp),
containerColor = if (selectedTool == ElementType.PATH) MaterialTheme.colorScheme.primaryContainer
else MaterialTheme.colorScheme.surfaceVariant
) {
Icon(Icons.Default.Edit, "Draw")
}
FloatingActionButton(
onClick = { selectedTool = ElementType.RECTANGLE },
modifier = Modifier.size(48.dp),
containerColor = if (selectedTool == ElementType.RECTANGLE) MaterialTheme.colorScheme.primaryContainer
else MaterialTheme.colorScheme.surfaceVariant
) {
Icon(Icons.Default.CropSquare, "Rectangle")
}
FloatingActionButton(
onClick = { selectedTool = ElementType.OVAL },
modifier = Modifier.size(48.dp),
containerColor = if (selectedTool == ElementType.OVAL) MaterialTheme.colorScheme.primaryContainer
else MaterialTheme.colorScheme.surfaceVariant
) {
Icon(Icons.Default.Circle, "Circle")
}
}
}
) { padding ->
Box(
modifier = Modifier
.fillMaxSize()
.padding(padding)
.background(MaterialTheme.colorScheme.background)
.pointerInput(Unit) {
detectTapGestures(
onPress = { tapOffset ->
isDrawing = true
currentPath.moveTo(tapOffset.x, tapOffset.y)
tryAwaitRelease()
isDrawing = false
}
)
}
) {
// Main drawing canvas
Canvas(
modifier = Modifier
.fillMaxSize()
.transformable(panZoomState)
.pointerInput(Unit) {
detectDragGestures(
onDragStart = { dragOffset ->
if (selectedTool == ElementType.PATH) {
isDrawing = true
currentPath.moveTo(dragOffset.x, dragOffset.y)
}
},
onDrag = { change, dragAmount ->
change.consume()
when (selectedTool) {
ElementType.PATH -> {
val x = change.position.x
val y = change.position.y
currentPath.lineTo(x, y)
}
else -> {
// Handle other element types
}
}
},
onDragEnd = {
isDrawing = false
// Add the completed path to the list
if (currentPath.getBounds().isEmpty.not()) {
paths.add(PluckablePath(currentPath, currentColor, strokeWidth))
currentPath = Path()
}
}
)
}
) {
// Apply transformations
scale(scale.value, scale.value) {
translate(offset.value.x, offset.value.y) {
drawGrid()
}
}

// Draw all elements
scale(scale.value, scale.value) {
translate(offset.value.x, offset.value.y) {
elements.forEach { element ->
when (element.type) {
ElementType.PATH -> {
drawPath(
path = element.path.toPath(),
color = element.color,
style = Stroke(
width = element.strokeWidth,
cap = StrokeCap.Round,
join = StrokeJoin.Round
)
)
}
ElementType.RECTANGLE -> {
val bounds = element.path.toPath().getBounds()
drawRect(
color = element.color,
topLeft = Offset(bounds.left, bounds.top),
size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
style = Stroke(width = element.strokeWidth)
)
}
ElementType.OVAL -> {
val bounds = element.path.toPath().getBounds()
drawOval(
color = element.color,
topLeft = Offset(bounds.left, bounds.top),
size = androidx.compose.ui.geometry.Size(bounds.width, bounds.height),
style = Stroke(width = element.strokeWidth)
)
}
else -> {}
}
}
}
}

// Draw current path being drawn
if (isDrawing) {
scale(scale.value, scale.value) {
translate(offset.value.x, offset.value.y) {
drawPath(
path = currentPath,
color = currentColor,
style = Stroke(
width = strokeWidth / scale.value,
cap = StrokeCap.Round,
join = StrokeJoin.Round
)
)
}
}
}

// Draw all animated paths
for (i in paths.indices) {
val path = paths[i]
val animatedPath = animatedPaths[i] ?: continue
scale(animatedPath.scale, animatedPath.scale) {
translate(animatedPath.offset.x, animatedPath.offset.y) {
drawPath(
path = path.path,
color = path.color.copy(alpha = path.alpha),
style = Stroke(width = path.strokeWidth)
)
}
}
}
}

// Toolbar
CanvasToolbar(
onColorSelected = { color ->
currentColor = color
},
onStrokeWidthSelected = { width ->
strokeWidth = width
},
onClear = {
paths.clear()
animatedPaths.clear()
}
)
}
}
}

// Extension to draw grid
private fun DrawScope.drawGrid() {
val gridSpacing = 50f
val strokeWidth = 1f
val color = Color.Gray.copy(alpha = 0.3f)

// Draw vertical lines
for (x in 0..size.width.toInt() step gridSpacing.toInt()) {
drawLine(
color = color,
start = Offset(x.toFloat(), 0f),
end = Offset(x.toFloat(), size.height),
strokeWidth = strokeWidth
)
}

// Draw horizontal lines
for (y in 0..size.height.toInt() step gridSpacing.toInt()) {
drawLine(
color = color,
start = Offset(0f, y.toFloat()),
end = Offset(size.width, y.toFloat()),
strokeWidth = strokeWidth
)
