package com.cynergisuite.middleware.area

import io.micronaut.aop.Around

// to generate the below run cyn gen module-type-dis | xclip -sel clip.
// that will generate the TypeListing objects and the below enum
// will also need to have a fully migrated test database running locally
enum class ModuleAccessType(
   val type: ModuleType,
) {
   APADD(TypeListing.APADD),
   APSHO(TypeListing.APSHO),
   APCHG(TypeListing.APCHG),
   APDEL(TypeListing.APDEL),
   APCHECK(TypeListing.APCHECK),
   APCHKRPT(TypeListing.APCHKRPT),
   APCLEAR(TypeListing.APCLEAR),
   APSEL(TypeListing.APSEL),
   APPREVUE(TypeListing.APPREVUE),
   APVOID(TypeListing.APVOID),
   APAGERPT(TypeListing.APAGERPT),
   APRPT(TypeListing.APRPT),
   CASHOUT(TypeListing.CASHOUT),
   APSTATUS(TypeListing.APSTATUS),
   POADD(TypeListing.POADD),
   POCHG(TypeListing.POCHG),
   PODEL(TypeListing.PODEL),
   POLST(TypeListing.POLST),
   POPURGE(TypeListing.POPURGE),
   POSHO(TypeListing.POSHO),
   POINLOAD(TypeListing.POINLOAD),
   POUPDT(TypeListing.POUPDT),
   SPOADD(TypeListing.SPOADD),
   SPOLST(TypeListing.SPOLST),
   POCAN(TypeListing.POCAN),
   POCOPY(TypeListing.POCOPY),
   INVORDMT(TypeListing.INVORDMT),
   INVCRED(TypeListing.INVCRED),
   INVAVAIL(TypeListing.INVAVAIL),
   POSTAT(TypeListing.POSTAT),
   PODLST(TypeListing.PODLST),
   POSTAT1(TypeListing.POSTAT1),
   PODSQLST(TypeListing.PODSQLST),
   ITEMMNTS(TypeListing.ITEMMNTS),
   VENDOR(TypeListing.VENDOR),
   PODETCHG(TypeListing.PODETCHG),
   POREC(TypeListing.POREC),
   PORECLST(TypeListing.PORECLST),
   PORECRPT(TypeListing.PORECRPT),
   PORPT(TypeListing.PORPT),
   POWRKSHT(TypeListing.POWRKSHT),
   QUOTERPT(TypeListing.QUOTERPT),
   VDRQUOTE(TypeListing.VDRQUOTE),
   SPOPRT(TypeListing.SPOPRT),
   STKRERDR(TypeListing.STKRERDR),
   GETSTKLV(TypeListing.GETSTKLV),
   PINVBC(TypeListing.PINVBC),
   PINORDRT(TypeListing.PINORDRT),
   APGLRPT(TypeListing.APGLRPT),
   ADDCOMP(TypeListing.ADDCOMP),
   CHGCOMP(TypeListing.CHGCOMP),
   DELCOMP(TypeListing.DELCOMP),
   LSTCOMP(TypeListing.LSTCOMP),
   PRTCOMP(TypeListing.PRTCOMP),
   SHOCOMP(TypeListing.SHOCOMP),
   SETSYS(TypeListing.SETSYS),
   POPARAMS(TypeListing.POPARAMS),
   APLST(TypeListing.APLST),
   ADDVEND(TypeListing.ADDVEND),
   CHGVEND(TypeListing.CHGVEND),
   DELVEND(TypeListing.DELVEND),
   LSTVEND(TypeListing.LSTVEND),
   PRTVEND(TypeListing.PRTVEND),
   DEFVEND(TypeListing.DEFVEND),
   SHOVEND(TypeListing.SHOVEND),
   SHIPVIA(TypeListing.SHIPVIA),
   ADDVIA(TypeListing.ADDVIA),
   CHGVIA(TypeListing.CHGVIA),
   DELVIA(TypeListing.DELVIA),
   PRTVIA(TypeListing.PRTVIA),
   SHOVIA(TypeListing.SHOVIA),
   APPURGE(TypeListing.APPURGE),
   APPARAMS(TypeListing.APPARAMS),
   ADDACCT(TypeListing.ADDACCT),
   CHGACCT(TypeListing.CHGACCT),
   DELACCT(TypeListing.DELACCT),
   LSTACCT(TypeListing.LSTACCT),
   PRTACCT(TypeListing.PRTACCT),
   CPYACCT(TypeListing.CPYACCT),
   SHOACCT(TypeListing.SHOACCT),
   ADDBANK(TypeListing.ADDBANK),
   CHGBANK(TypeListing.CHGBANK),
   DELBANK(TypeListing.DELBANK),
   LSTBANK(TypeListing.LSTBANK),
   PRTBANK(TypeListing.PRTBANK),
   SHOBANK(TypeListing.SHOBANK),
   GLPARAMS(TypeListing.GLPARAMS),
   ADDLAY(TypeListing.ADDLAY),
   CHGLAY(TypeListing.CHGLAY),
   DELLAY(TypeListing.DELLAY),
   FORMLAY(TypeListing.FORMLAY),
   PRTLAY(TypeListing.PRTLAY),
   CPYLAY(TypeListing.CPYLAY),
   SHOLAY(TypeListing.SHOLAY),
   ADDGLCOD(TypeListing.ADDGLCOD),
   CHGGLCOD(TypeListing.CHGGLCOD),
   DELGLCOD(TypeListing.DELGLCOD),
   LSTGLCOD(TypeListing.LSTGLCOD),
   PRTGLCOD(TypeListing.PRTGLCOD),
   SHOGLCOD(TypeListing.SHOGLCOD),
   ADDAPDST(TypeListing.ADDAPDST),
   CHGAPDST(TypeListing.CHGAPDST),
   DELAPDST(TypeListing.DELAPDST),
   SHOAPDST(TypeListing.SHOAPDST),
   PRTAPDST(TypeListing.PRTAPDST),
}

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FUNCTION)
@Around
// TODO need to figure out how to wire this into the AOP system @Type(AccessControlService::class)
annotation class ModuleAccess(
   val module: ModuleAccessType
)