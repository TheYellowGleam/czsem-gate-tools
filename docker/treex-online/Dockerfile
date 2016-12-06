#docker build -t="datlowe/treex-online" treex-online

FROM datlowe/centos7_2_jdk8
MAINTAINER Jan Dedek <http://github.com/dedekj>

RUN yum install -y perl && yum clean all


ENV \
TREEX_INST=/app/treex \
TREEX_GIT=${TREEX_INST}/treex_git \ 
TREEX_CONFIG=$TREEX_INST \
PATH=${TREEX_GIT}/bin:${TREEX_INST}/perl5/bin:${PATH} \
PERL5LIB=${TREEX_GIT}/lib:${TREEX_INST}/perl5/lib/perl5:${PERL5LIB} \
PERL_LOCAL_LIB_ROOT=${TREEX_INST}/perl5:${PERL_LOCAL_LIB_ROOT} \
PERL_MB_OPT="--install_base ${TREEX_INST}/perl5" \
PERL_MM_OPT="INSTALL_BASE=${TREEX_INST}/perl5" 

