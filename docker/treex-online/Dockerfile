#docker build -t="datlowe/treex-online" treex-online

FROM datlowe/centos7_2_jdk8
MAINTAINER Jan Dedek <http://github.com/dedekj>

RUN yum install -y perl perl-XML-Parser && yum clean all

ENV TREEX_INST=/app/treex

ENV \
TREEX_GIT=${TREEX_INST}/treex_git \ 
TREEX_CONFIG=$TREEX_INST \
PATH=${TREEX_INST}/treex_git/bin:${TREEX_INST}/perl5/bin:${PATH} \
PERL5LIB=${TREEX_INST}/treex_git/lib:${TREEX_INST}/perl5/lib/perl5:${PERL5LIB} \
PERL_LOCAL_LIB_ROOT=${TREEX_INST}/perl5:${PERL_LOCAL_LIB_ROOT} \
PERL_MB_OPT="--install_base ${TREEX_INST}/perl5" \
PERL_MM_OPT="INSTALL_BASE=${TREEX_INST}/perl5" 

RUN mkdir /app; cd /app; curl -L https://github.com/datlowe/czsem-gate-tools/releases/download/czsem-3.0-alpha2/treex-online-linux64-installed.tar.gz | tar zxv 
