# Build Verification Guide

This document outlines the steps to build and verify the Todo Android application.

## Prerequisites

1. **Android SDK**: Ensure Android SDK is installed with API level 34
2. **Java Development Kit**: JDK 8 or higher
3. **Gradle**: Will be handled by the Gradle wrapper

## Build Commands

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test
./gradlew connectedAndroidTest
```

## Build Outputs

### Debug APK Location
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK Location
```
app/build/outputs/apk/release/app-release.apk
```

## Verification Checklist

### 1. Package Name Verification
- [ ] Package name is set to `com.canme.todo`
- [ ] Application ID matches in build.gradle.kts
- [ ] Namespace is correctly configured

### 2. Build Configuration
- [ ] Debug build includes debug suffix
- [ ] Release build has ProGuard enabled
- [ ] Signing configuration is properly set
- [ ] Version code and name are correct

### 3. APK Analysis
- [ ] APK can be installed on device
- [ ] App launches successfully
- [ ] All features work as expected
- [ ] No crashes during basic usage

### 4. Performance Verification
- [ ] App starts within reasonable time
- [ ] UI is responsive
- [ ] Database operations are smooth
- [ ] Memory usage is acceptable

### 5. Functionality Testing
- [ ] Add new todos
- [ ] Mark todos as complete/incomplete
- [ ] Delete todos with confirmation
- [ ] Empty state displays correctly
- [ ] Dark mode works properly

### 6. Accessibility Testing
- [ ] Screen reader compatibility
- [ ] Proper content descriptions
- [ ] Keyboard navigation works
- [ ] Touch targets meet minimum size requirements

## Manual Testing Steps

### Basic Functionality
1. Install the APK on a device or emulator
2. Launch the app
3. Verify the empty state appears initially
4. Add a new todo item
5. Mark the todo as complete
6. Add multiple todos
7. Delete a todo item
8. Test long-press for delete confirmation
9. Test rotation and configuration changes
10. Test app backgrounding and foregrounding

### Theme Testing
1. Test light theme appearance
2. Switch to dark mode (system setting)
3. Verify dark theme colors are applied
4. Test dynamic colors on Android 12+ devices
5. Verify edge-to-edge display works correctly

### Accessibility Testing
1. Enable TalkBack (Android screen reader)
2. Navigate through the app using TalkBack
3. Verify all elements have proper descriptions
4. Test keyboard navigation if available
5. Verify touch targets are accessible

## Build Optimization

### Release Build Features
- **Code Obfuscation**: ProGuard removes unused code and obfuscates names
- **Resource Shrinking**: Removes unused resources
- **APK Optimization**: Compressed and optimized for distribution

### Performance Considerations
- **Startup Time**: App should launch within 2-3 seconds
- **Memory Usage**: Should stay under 100MB for normal usage
- **APK Size**: Target under 10MB for the basic todo app
- **Battery Usage**: Minimal background activity

## Troubleshooting

### Common Build Issues
1. **Gradle Wrapper Issues**: Ensure gradle wrapper files are present and executable
2. **SDK Path**: Verify ANDROID_HOME environment variable is set
3. **Java Version**: Ensure compatible JDK version is installed
4. **Dependency Conflicts**: Check for version conflicts in dependencies

### Runtime Issues
1. **Crashes on Launch**: Check ProGuard rules for missing keep directives
2. **Database Issues**: Verify Room configuration and migrations
3. **UI Issues**: Test on different screen sizes and orientations
4. **Performance Issues**: Profile with Android Studio tools

## Distribution Preparation

### For Production Release
1. **Generate Release Keystore**: Create a proper release signing key
2. **Update Signing Config**: Use production keystore instead of debug
3. **Version Management**: Increment version code and name
4. **Testing**: Perform thorough testing on multiple devices
5. **Store Listing**: Prepare app store metadata and screenshots

### Security Considerations
- Store signing keys securely
- Use environment variables for sensitive data
- Enable app signing by Google Play for additional security
- Implement certificate pinning if using network requests