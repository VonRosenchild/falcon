#
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# The java implementation to use. If JAVA_HOME is not found we expect java and jar to be in path
#export JAVA_HOME=

# any additional java opts you want to set. This will apply to both client and server operations
#export FALCON_OPTS=

# any additional java opts that you want to set for client only
#export FALCON_CLIENT_OPTS=

# java heap size we want to set for the client. Default is 1024MB
#export FALCON_CLIENT_HEAP=

# any additional opts you want to set for prisim service.
#export FALCON_PRISM_OPTS=

# java heap size we want to set for the prisim service. Default is 1024MB
#export FALCON_PRISM_HEAP=

# any additional opts you want to set for falcon service.
#export FALCON_SERVER_OPTS=

# java heap size we want to set for the falcon server. Default is 1024MB
#export FALCON_SERVER_HEAP=

# What is is considered as falcon home dir. Default is the base locaion of the installed software
#export FALCON_HOME_DIR=

# Where log files are stored. Defatult is logs directory under the base install location
#export FALCON_LOG_DIR=

# Where pid files are stored. Defatult is logs directory under the base install location
#export FALCON_PID_DIR=

# where the falcon active mq data is stored. Defatult is logs/data directory under the base install location
#export FALCON_DATA_DIR=

# Where do you want to expand the war file. By Default it is in /server/webapp dir under the base install dir.
#export FALCON_EXPANDED_WEBAPP_DIR=
