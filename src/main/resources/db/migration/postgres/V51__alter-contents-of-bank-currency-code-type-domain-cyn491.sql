UPDATE public.bank_currency_code_type_domain
   SET description = 'U.S. Dollar', localization_code = 'united.states.dollar'
   WHERE id = 1;

UPDATE public.bank_currency_code_type_domain
   SET description = 'Canadian Dollar', localization_code = 'canadian.dollar'
   WHERE id = 2;
