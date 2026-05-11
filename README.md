## Commands

| Command | Aliases | Description |
| --- | --- | --- |
| `/picturelogin reload` | `/piclogin`, `/plogin`, `/pl` | Reload the plugin configuration. |
| `/picturelogin version` | same | Print plugin version. |
| `/picturelogin language` | same | Change the plugin language. |
| `/picturelogin debug` | same | Run the debug command. |
| `/picturelogin help` | same | List commands. |

## Permissions

| Node | Default | Purpose |
| --- | --- | --- |
| `picturelogin.use` | true | Allow use of PictureLogin commands. |
| `picturelogin.show` | true | See the login picture message. |
| `picturelogin.reload` | op | Reload the configuration. |
| `picturelogin.language` | op | Change the language. |
| `picturelogin.debug` | op | Use the debug command. |
| `picturelogin.group.vip` | false | Apply the VIP custom message. |
| `picturelogin.group.admin` | op | Apply the admin custom message. |
| `picturelogin.effect.rainbow` | false | Use the rainbow text effect. |
| `picturelogin.effect.gradient` | false | Use the gradient text effect. |

## Placeholders

| Placeholder | Returns |
| --- | --- |
| `%picturelogin_avatar_<n>%` | Line `<n>` of the joining player's avatar. |
| `%picturelogin_player_avatar_<n>_<player>%` | Line `<n>` of the given player's avatar. |
