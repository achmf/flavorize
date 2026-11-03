# Flavorize

An Android app for sharing, finding and learning recipes. Users can create their own recipes, bookmark favorites and comment on other people's recipes. Final project for the Android Intermediate class at KSM Android UPN Veteran Jakarta.

## Features

- Login with Google
- Random and global recipes from [TheMealDB](https://www.themealdb.com)
- Create, edit and delete your own recipes
- Bookmarks and comments
- Search
- Offline drafts
- Paged recipe lists

## Tech stack

- Kotlin and XML layouts
- MVVM: ViewModel, LiveData and repositories
- Navigation Component, bottom navigation and navigation drawer
- Firebase Auth and Firestore
- Supabase Storage for images
- Room for local drafts
- Paging 3
- Retrofit and Glide

## Getting started

1. Open the project in Android Studio.
2. Let Gradle sync.
3. Run the `app` configuration on a device or emulator (Android 9, API 28, or newer).

Google sign-in, Firestore and Supabase Storage depend on the project's own Firebase and Supabase setup. To use your own, replace `app/google-services.json` and the values in `data/SupabaseStorageRepository.kt`.

## Project structure

```
app/src/main/java/com/example/flavorize/
  data/            Firestore, Supabase, TheMealDB and Room drafts
  ui/activities/   Login, main, recipe detail, forms, bookmarks, drafts, profile
  ui/fragments/    Home, Recipes and My Recipes
```
