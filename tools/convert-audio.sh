#/bin/bash
find "$1" -name '*.ogg' -exec sh -c 'ffmpeg -i "$0" "${0%%.ogg}.mp3"' {} \;
exit;
