package com.cynergisuite.middleware.vendor

import groovy.transform.CompileStatic
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton


class VendorTypeFactory {

   private static final List<VendorType> vendorType = [
      new VendorType(
         1,
         1,
         "Rents",
         "rents"
      ),
      new VendorType(
         2,
         2,
         "Royalties",
         "royalties"
      ),
      new VendorType(
         3,
         3,
         "Other Income",
         "other"
      ),
      new VendorType(
         4,
         4,
         "Federal Income Tax Withheld",
         "federal.income.tax.withheld"
      ),
      new VendorType(
         5,
         5,
         "Fishing Boat Proceeds",
         "fishing.boat.proceeds"
      ),
      new VendorType(
         6,
         6,
         "Medical and Health Care Payments",
         "medical.health.care.payments"
      ),
      new VendorType(
         7,
         7,
         "Payer made direct sales totaling \$5,000 or more of consumer products to recipient for resale",
         "payer.made.direct.sales.consumer.products"
      ),
      new VendorType(
         8,
         8,
         "Substitute payments in lieu of dividends or interest",
         "substitute.payments"
      ),
      new VendorType(
         9,
         9,
         "Crop Insurance Proceeds",
         "crop.insurance.proceeds"
      ),
      new VendorType(
         10,
         10,
         "Gross proceeds paid to an attorney",
         "gross.proceeds.attorney"
      ),
      new VendorType(
         11,
         11,
         "Fish purchased for resale",
         "fish.purchased.resale"
      ),
      new VendorType(
         12,
         12,
         "Section 409A Deferrals",
         "section.409A.deferrals"
      ),
      new VendorType(
         13,
         13,
         "Excess Golden Parachute Payments",
         "excess.golden.parachute"
      ),
      new VendorType(
         14,
         14,
         "Nonqualified Deferred Compensation",
         "nonqualified.deferred.compensation"
      ),
      new VendorType(
         15,
         15,
         "State Tax Withheld",
         "state.tax.withheld"
      ),
      new VendorType(
         16,
         16,
         "State/Payers State No",
         "state.payers.state.no"
      ),
      new VendorType(
         17,
         17,
         "State Income",
         "state.income"
      ),
      new VendorType(
         101,
         101,
         "Nonemployee Compensation",
         "nonemployee.compensation"
      ),
      new VendorType(
         102,
         102,
         "Payer Made Direct Sales Totaling \$5000 or more",
         "payer.made.direct.sales"
      ),
      new VendorType(
         103,
         103,
         "Reserved for Future Use",
         "reserved.future.use"
      ),
      new VendorType(
         104,
         104,
         "Federal Income Tax Withheld",
         "federal.income.tax.withheld"
      ),
      new VendorType(
         105,
         105,
         "State Tax Withheld",
         "state.tax.withheld.box.105"
      ),
      new VendorType(
         106,
         106,
         "State/Payers State No",
         "state.payers.state.no.box.106"
      ),
      new VendorType(
         107,
         107,
         "State Income",
         "state.income.box.107"
      )
   ]

   static VendorType random() {
      return vendorType.random()
   }

   static List<VendorType> predefined() {
      return vendorType
   }
}

@Singleton
@CompileStatic
@Requires(env = ["develop", "test"])
class VendorTypeFactoryService {

   def random() {
      return VendorTypeFactory.random()
   }

   def predefined() {
      return VendorTypeFactory.predefined()
   }
}
