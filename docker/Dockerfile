############################################################################
#    Copyright (c) 2018 European Molecular Biology Laboratory
#
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
############################################################################

FROM ubuntu:18.04

ENV JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64

RUN apt-get update \
    && apt-get install -y wget openjdk-11-jdk maven git coreutils hmmer python-numpy python-qt4 python-lxml python-six \
     python-pip python-biopython python-requests python3 ncbi-data libdw1 libpcre3-dev
RUN pip install --upgrade ete3

COPY scripts /opt/scripts/bin
RUN chmod 775 /opt/scripts/bin/*.sh

RUN /opt/scripts/bin/update-taxonomy-cache.sh
RUN /opt/scripts/bin/download-interproscan.sh
RUN /opt/scripts/bin/download-unifire.sh

RUN mkdir /volume
VOLUME /volume

CMD /opt/scripts/bin/unifire-workflow.sh
