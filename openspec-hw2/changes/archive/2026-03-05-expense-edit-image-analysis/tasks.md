## 1. Dependencies & Config

- [x] 1.1 Add Coil 3 dependency to `gradle/libs.versions.toml` and `app/build.gradle.kts` (`io.coil-kt.coil3:coil-compose`)
- [x] 1.2 Add `<uses-permission android:name="android.permission.CAMERA" />` to `AndroidManifest.xml`
- [x] 1.3 Add FileProvider configuration in `AndroidManifest.xml` for camera capture (temp file URI)

## 2. DTO & API Layer

- [x] 2.1 Add `NextExpensePhoto` data class to `NextModels.kt` (id, entryId, filename, storagePath, fileSize, mimeType, createdAt)
- [x] 2.2 Add `NextParsePreview` data class to `NextModels.kt` (merchant, date, currency, tags, items, subtotal, tax, tip, totalAmount)
- [x] 2.3 Add `NextExpensePhotoListResponse`, `NextExpensePhotoResponse`, `NextParsePreviewResponse` to `NextModels.kt`
- [x] 2.4 Update `NextExpenseDetailResponse` to include `photos: List<NextExpensePhoto>` field
- [x] 2.5 Add `uploadExpensePhoto(entryId, uri, context)` to `NextApiService` using Ktor `submitFormWithBinaryData`
- [x] 2.6 Add `deleteExpensePhoto(photoId)` to `NextApiService` (`DELETE /api/expenses/photos/:photo_id`)
- [x] 2.7 Add `parseReceiptPreview(imageBase64)` to `NextApiService` (`POST /api/expenses/parse-preview`, 120s timeout)

## 3. Detail/Edit Page — ViewModel

- [x] 3.1 Create `ExpenseDetailViewModel` with states: loading, detail data, edit fields, save/delete status, error
- [x] 3.2 Implement `loadDetail(expenseId)` — call `getExpenseById`, populate edit fields
- [x] 3.3 Implement `saveChanges()` — call `updateExpense` with changed fields only
- [x] 3.4 Implement `deleteExpense()` — call `deleteExpense`, emit navigation-back event
- [x] 3.5 Use `TextFieldValue` for notes field (Chinese IME compatibility)
- [x] 3.6 Register `ExpenseDetailViewModel` in `AppModule.kt` with Koin (`viewModel { params -> ExpenseDetailViewModel(params.get(), get(), get()) }`)

## 4. Detail/Edit Page — UI

- [x] 4.1 Create `ExpenseDetailScreen.kt` with Scaffold, TopAppBar (back + delete), and scrollable content
- [x] 4.2 Implement edit form: amount (decimal keyboard), date (DatePicker), notes (TextFieldValue), currency (chip group), tags (FlowRow chips + custom input)
- [x] 4.3 Display AI-parsed item lines section (read-only list: name, qty, unit_price, amount, specs)
- [x] 4.4 Implement save button with loading state and error handling
- [x] 4.5 Implement delete with confirmation dialog

## 5. Photo Management — UI & Logic

- [x] 5.1 Add photo section to `ExpenseDetailScreen`: horizontal scrollable row of thumbnails + "add photo" button
- [x] 5.2 Implement photo picker launcher (`rememberLauncherForActivityResult` with `PickVisualMedia`)
- [x] 5.3 Implement camera capture launcher (`rememberLauncherForActivityResult` with `TakePicture`) with runtime CAMERA permission request
- [x] 5.4 Add source selection bottom sheet or dialog (gallery / camera)
- [x] 5.5 Implement photo upload in ViewModel — read URI via ContentResolver, call `uploadExpensePhoto`, refresh detail
- [x] 5.6 Implement client-side 10MB file size check before upload
- [x] 5.7 Display photo thumbnails using Coil with auth cookie header (`ImageRequest.Builder.addHeader("Cookie", ...)`)
- [x] 5.8 Implement full-screen photo viewer (overlay dialog with dismiss)
- [x] 5.9 Implement photo delete with confirmation → call `deleteExpensePhoto`, refresh detail

## 6. AI Receipt Scan Flow

- [x] 6.1 Add "拍照记账" entry point: FAB long-press or second button on ExpenseScreen
- [x] 6.2 Implement image → base64 conversion utility (read URI, encode to base64 string)
- [x] 6.3 Add parse preview state to `ExpenseDetailViewModel` (preview data, loading, error)
- [x] 6.4 Implement `parseReceipt(uri)` in ViewModel — convert to base64, call `parseReceiptPreview`, populate preview state
- [x] 6.5 Create parse preview UI: pre-filled edit form (amount, date, notes=merchant, tags, currency) + read-only items list + confirm/cancel buttons
- [x] 6.6 Implement confirm flow: call `createExpense` with items array, then `uploadExpensePhoto` for the original image
- [x] 6.7 Handle parse timeout (120s) and failure: show error message with manual input fallback
- [x] 6.8 Handle partial failure: expense created but photo upload fails → navigate to list with warning toast

## 7. Navigation & Integration

- [x] 7.1 Add `expense/{expenseId}` route to `ErgouNavigation.kt` with `navArgument`
- [x] 7.2 Add `expense/new-scan` route for AI scan flow (no existing expense ID)
- [x] 7.3 Update `ExpenseScreen` — make expense items clickable, navigate to `expense/{id}`
- [x] 7.4 Add "拍照记账" navigation from ExpenseScreen to `expense/new-scan`

## 8. Manual Testing

- [ ] 8.1 Test: open expense detail, verify all fields load correctly (amount, date, notes, tags, currency, items)
- [ ] 8.2 Test: edit amount + notes, save, verify list reflects changes
- [ ] 8.3 Test: delete expense from detail page, verify list refreshes
- [ ] 8.4 Test: upload photo from gallery, verify thumbnail appears
- [ ] 8.5 Test: capture photo with camera, verify upload and display
- [ ] 8.6 Test: scan receipt photo, verify AI preview shows parsed data, confirm saves correctly
- [ ] 8.7 Test: AI parse timeout/failure, verify fallback to manual input
- [ ] 8.8 Test: delete uploaded photo, verify removal
- [ ] 8.9 Test: network offline scenarios — detail load, save, upload all show appropriate errors
