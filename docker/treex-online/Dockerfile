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


RUN echo "Bohužel jsem tento tutorial nedokončil." |  treex -Lcs Read::Sentences W2A::CS::Tokenize W2A::CS::TagMorphoDiTa lemmatize=1 W2A::CS::FixMorphoErrors A2N::CS::NameTag W2A::CS::ParseMSTAdapted W2A::CS::FixAtreeAfterMcD W2A::CS::FixIsMember W2A::CS::FixReflexiveTantum W2A::CS::FixReflexivePronouns A2N::CS::SimpleRuleNER A2T::CS::MarkEdgesToCollapse A2T::BuildTtree A2T::RehangUnaryCoordConj A2T::SetIsMember A2T::CS::SetCoapFunctors A2T::FixIsMember A2T::MarkParentheses A2T::MoveAuxFromCoordToMembers A2T::CS::MarkClauseHeads A2T::CS::MarkRelClauseHeads A2T::CS::MarkRelClauseCoref A2T::CS::FixTlemmas A2T::CS::FixNumerals A2T::SetNodetype A2T::CS::SetFormeme use_version=2 fix_prep=0 A2T::CS::SetDiathesis A2T::CS::SetFunctors memory=2g A2T::CS::SetMissingFunctors A2T::SetNodetype A2T::FixAtomicNodes A2T::CS::SetGrammatemes A2T::CS::MarkReflexivePassiveGen A2T::CS::AddPersPron T2T::SetClauseNumber A2T::CS::MarkReflpronCoref Write::CoNLLX
#this command is important: Stahuje ("kešuje") spoustu dat z webu, která se jinak stáhnou až při prvním použití treex-u (cs treex full analysis), takže to první použití je pak pomalejší a musím být on-line
