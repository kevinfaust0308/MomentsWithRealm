# Moments

## Keep memories close and Moments closer!
### Currently being migrating from built-in SQLite to Realm, fixing image save/load bugs, and storage optimization :raised_hands:
Playstore download link: https://goo.gl/JTRCcM

#### Update December 30, 2016
- VERSION 2 RELEASED ON PLAY STORE
- improved how recyclerview and adapter interact
- updated app screenshots for publish

#### Update December 29, 2016
- fixed way to store photo's uri (absolute file path)
- deleting entries from database will delete the associated image file
- when updating an entry, original image is not deleted unless a new image is selected and submitted
- removed unnecessary bloat in settings popup
- removed option to delete all entries
- gave greater spacing to all elements for friendlier UX
- search layout now slides downwards with filter options stacked (submit button and text were previously being cut off)
- other UI and bug fixes


#### Update December 28, 2016
- under progress of moving from built-in SQLite to Realm
- speed improvements and bugs fixed
- chosen image files to use will no longer be kept if user removes preview image or exits adding/updating Moment fragment (dramatically reduced app usage)
- migrated from Picasso to Glide which led to images being load faster (and fixed a glitch where images would not load on app restart)

#### Update June 29, 2016
- VERSION 1 RELEASED ON PLAY STORE
