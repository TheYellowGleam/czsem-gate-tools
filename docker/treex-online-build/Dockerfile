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
Data::Dumper


RUN git clone http://github.com/ufal/treex.git ${TREEX_GIT}
ENV PATH=${TREEX_GIT}/bin:$PATH \
PERL5LIB=${TREEX_GIT}/lib:$PERL5LIB

RUN echo "Bohužel jsem tento tutorial nedokončil." |  treex -Lcs Read::Sentences W2A::CS::Tokenize W2A::CS::TagMorphoDiTa lemmatize=1 W2A::CS::FixMorphoErrors A2N::CS::NameTag W2A::CS::ParseMSTAdapted W2A::CS::FixAtreeAfterMcD W2A::CS::FixIsMember W2A::CS::FixReflexiveTantum W2A::CS::FixReflexivePronouns A2N::CS::SimpleRuleNER A2T::CS::MarkEdgesToCollapse A2T::BuildTtree A2T::RehangUnaryCoordConj A2T::SetIsMember A2T::CS::SetCoapFunctors A2T::FixIsMember A2T::MarkParentheses A2T::MoveAuxFromCoordToMembers A2T::CS::MarkClauseHeads A2T::CS::MarkRelClauseHeads A2T::CS::MarkRelClauseCoref A2T::CS::FixTlemmas A2T::CS::FixNumerals A2T::SetNodetype A2T::CS::SetFormeme use_version=2 fix_prep=0 A2T::CS::SetDiathesis A2T::CS::SetFunctors memory=2g A2T::CS::SetMissingFunctors A2T::SetNodetype A2T::FixAtomicNodes A2T::CS::SetGrammatemes A2T::CS::MarkReflexivePassiveGen A2T::CS::AddPersPron T2T::SetClauseNumber A2T::CS::MarkReflpronCoref Write::CoNLLX
#this command is important: Stahuje ("kešuje") spoustu dat z webu, která se jinak stáhnou až při prvním použití treex-u (cs treex full analysis), takže to první použití je pak pomalejší a musím být on-line


#czsem treex online
RUN curl -L https://github.com/dedekj/czsem-gate-tools/archive/master.tar.gz | tar zxvf - czsem-gate-tools-master/modules/treex-gate-plugin/treex_online/ \
&& mv czsem-gate-tools-master/modules /app/czsem \
&& rm -r czsem-gate-tools-master 
