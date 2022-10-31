#!/usr/bin/env bash

#
# (c) Copyright 2022 Birch Solutions Inc. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

tag="$(git describe --exact-match --tags HEAD 2> /dev/null || :)"

# if the current commit is tagged but we're not on a tag in Circle, then
# should then ignore the tag
if [[ -n "$tag" && -z "$CIRCLE_TAG" ]]; then
	exclude_param="--exclude $tag"
fi
git describe --tags --always --first-parent $exclude_param