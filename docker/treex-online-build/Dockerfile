#docker build -t="datlowe/treex-online-build" treex-online-build

FROM datlowe/centos7_2_jdk8
MAINTAINER Jan Dedek <http://github.com/dedekj>

RUN yum groupinstall -y @development \
&& yum install -y perl-devel perl-CPAN libxml2-devel zlib1g-devel \
&& yum clean all

ENV TREEX_INST=/app/treex

RUN curl -L http://cpanmin.us | perl - -l $TREEX_INST/perl5 App::cpanminus local::lib
 
ENV \
PATH=${TREEX_INST}/perl5/bin:${PATH} \
PERL5LIB=${TREEX_INST}/perl5/lib/perl5:${PERL5LIB} \
PERL_LOCAL_LIB_ROOT=${TREEX_INST}/perl5:${PERL_LOCAL_LIB_ROOT} \
PERL_MB_OPT="--install_base ${TREEX_INST}/perl5" \
PERL_MM_OPT="INSTALL_BASE=${TREEX_INST}/perl5" \
TREEX_CONFIG=$TREEX_INST \
TREEX_GIT=${TREEX_INST}/treex_git 

ADD config.yaml $TREEX_CONFIG/

RUN cpanm -f PerlIO::Util \
&& cpanm Fatal XML::LibXML Ufal::MorphoDiTa Ufal::NameTag \
Lingua::Interset URI::Find Cache::LRU YAML::Tiny Graph String::Util MooseX::Role::AttributeOverride \
Treex::Core Treex::EN Treex::CS \
Net::Address::IP::Local RPC::XML RPC::XML::Server Set::Light Sys::Hostname \
Module::ExtractUse \
Data::Dumper Error Thread::Queue


RUN git clone http://github.com/ufal/treex.git ${TREEX_GIT}
ENV PATH=${TREEX_GIT}/bin:$PATH \
PERL5LIB=${TREEX_GIT}/lib:$PERL5LIB


#czsem treex online
RUN curl -L https://github.com/dedekj/czsem-gate-tools/archive/master.tar.gz | tar zxvf - czsem-gate-tools-master/modules/treex-gate-plugin/treex_online/ \
&& mv czsem-gate-tools-master/modules /app/czsem \
&& rm -r czsem-gate-tools-master 


RUN rm -rf $TREEX_GIT/.git ; rm -rf /app/treex/share/* ; cd app ; tar -czvf /treex-online-linux64-installed.tar.gz * 