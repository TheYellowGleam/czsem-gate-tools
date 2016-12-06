#docker build -t="datlowe/centos7_2_jdk8" centos7_2_jdk8

FROM centos:7.2.1511
MAINTAINER Jan Dedek <http://github.com/dedekj>

ENV JAVA_HOME=/usr/java/default

RUN curl -LO http://download.oracle.com/otn-pub/java/jdk/8u111-b14/jdk-8u111-linux-x64.rpm \
-H 'Cookie: oraclelicense=accept-securebackup-cookie' \
&& rpm -i jdk-8u111-linux-x64.rpm \
&& rm -f jdk-8u111-linux-x64.rpm \
&& yum clean all

RUN yum -y update; yum clean all

