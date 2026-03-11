# Next Gaps: Health Feature

## Status
**Next has NO health API.** All health data is frontend-only static JavaScript (`health-data.js`, 1592 lines).
The ergou API calls to `/api/health/categories`, `/api/health/items`, `/api/health/search` will always fail and fall back to local `HealthData`.

## What Ergou Has (After This Fix)
- Rich local `HealthData.kt` with all content ported from Next's `health-data.js`
- 10 八段锦 exercises (8 main + prep + closing) with full descriptions, benefits, meridian associations, acupoints, video URLs
- 12 易筋经 exercises with full content
- 8 站桩 stances with unique "insights" (意念/骨骼排列/松沉/呼吸)
- 14 经络穴位 with detailed acupoint functions and indications
- API-first loading with local fallback (currently always falls back)
- Enhanced UI: bullet-point benefits, meridian intensity indicators, insights cards, video buttons

## What Next Frontend Has That Ergou Does NOT
1. **Canvas2D Meridian Visualization** (`health-renderer.js`, 692 lines)
   - Body outline with front/back view toggle
   - Meridian paths drawn as colored Bezier curves
   - Animated "qi particles" flowing along meridian paths
   - Interactive acupoint hit detection (tap to highlight + tooltip)
   - Pose interpolation for exercise animation (skeletal keyframes)

2. **Embedded Video Player**
   - 32 MP4 videos hosted at `/assets/videos/` (baduanjin/yijinjing/zhanzhuang)
   - HTML5 `<video>` element with native controls
   - Ergou opens videos in external browser (Intent.ACTION_VIEW)

3. **Body Outline Data** (normalized 0-1 coordinates)
   - Front and back view body contour points
   - 13 skeletal joints for pose deformation
   - Meridian path coordinates (front + back)

## Recommended Next API Additions
If Next adds health endpoints, they should match these specs:

### GET /api/health/categories
```json
{
  "success": true,
  "categories": [
    {
      "id": "baduanjin",
      "name": "八段锦",
      "description": "...",
      "intro": "..."
    }
  ]
}
```

### GET /api/health/items?category={id}
```json
{
  "success": true,
  "items": [
    {
      "id": "bdj_01",
      "category_id": "baduanjin",
      "name": "双手托天理三焦",
      "description": "...",
      "benefits": "功效1；功效2",
      "benefits_list": ["功效1", "功效2"],
      "meridians": ["三焦经", "肺经"],
      "key_points": ["外关(SJ-5)", "中府(LU-1)"],
      "meridian_details": [
        {"name": "三焦经", "intensity": "primary", "note": "双手上托直接拉伸三焦经"}
      ],
      "insights": [
        {"label": "意念", "content": "意守丹田"}
      ],
      "video_url": "/assets/videos/baduanjin/bdj-01.mp4"
    }
  ]
}
```

### GET /api/health/search?q={keyword}
Same response format as items endpoint, searches across all categories.

## Video Hosting
Videos are currently served as static files from Next's frontend directory.
For ergou to play them in-app (vs external browser), options:
1. **WebView** — load `https://next-boris.fly.dev/assets/videos/...` in a WebView with HTML5 video tag
2. **ExoPlayer** — direct MP4 URL playback (requires ExoPlayer dependency)
3. **Keep current** — Intent.ACTION_VIEW opens in system browser/player (simplest)

## Future: Meridian Visualization in Ergou
Options for bringing Canvas2D visualization to Android:
1. **WebView** — load Next's health page in a WebView (simplest, reuses existing code)
2. **Compose Canvas** — port the renderer to Compose `Canvas` (native, better UX, significant effort)
3. **Static SVG images** — pre-render meridian diagrams as SVG/PNG (medium effort, no interactivity)

Recommended: Start with **WebView** for quick parity, then consider native Canvas in a future sprint.
