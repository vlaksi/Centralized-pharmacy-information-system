<template>
  <v-row class="pa-5">
    <v-snackbar v-model="snackbar" :timeout="2000" bottom class="mb-5" right>
      {{ snackbarText }}
      <template v-slot:action="{ attrs }">
        <v-btn color="primary" text v-bind="attrs" @click="snackbar = false">Close</v-btn>
      </template>
    </v-snackbar>

    <v-spacer></v-spacer>

    <!-- Left column for filter & search -->
    <v-col sm="12" md="4" lg="3" xl="3" cols="12">
      <!-- Search card -->
      <v-card>
        <!--Toolbar of the card-->
        <v-toolbar color="primary" dark dense flat>
          <v-toolbar-title class="body-2">Search drugs</v-toolbar-title>
        </v-toolbar>
        <!--End of toolbar of the card-->

        <v-form class="pa-2">
          <v-text-field
            v-model="searchDrugField"
            prepend-icon="mdi-magnify"
            label="Name of the drug"
          ></v-text-field>
        </v-form>
      </v-card>
      <!-- End of the search card-->
      <!-- Filter card -->
      <v-card class="mt-5">
        <!--Toolbar of the card-->
        <v-toolbar color="primary" dark dense flat>
          <v-toolbar-title class="body-2">Filter drugs</v-toolbar-title>
        </v-toolbar>
        <!--End of toolbar of the card-->

        <v-form class="pa-2">
          <v-card-text>
            <h3>Drugs rating</h3>
          </v-card-text>
          <template v-if="isPharmacyAdmin">
            <v-checkbox
              class="ml-4 mr-4"
              v-model="onlyShowNotInPharm"
              primary
              label="Only show drugs that aren't available in the pharmacy"
            ></v-checkbox>
            <v-divider></v-divider>
            <br />
          </template>
          <v-range-slider :tick-labels="marks" class="ml-4 mr-4" v-model="markRange" step="25"></v-range-slider>
          <br />
          <v-divider></v-divider>
          <br />
          <v-select
            class="ml-4 mr-4"
            v-model="selectedTypeOfDrugs"
            :items="allTypeOfDrugs"
            label="Select type of the drug"
            multiple
            chips
            clearable
          ></v-select>
        </v-form>
      </v-card>
      <!-- End of the filter card -->
    </v-col>
    <!-- End of the left column for filter & search -->

    <!-- Right column for drugs preview-->
    <v-col sm="12" md="8" lg="6" xl="6" cols="12">
      <v-card elevation="4" class="pa-4 ml-10 mb-10" v-for="drug in filteredDrugs" :key="drug.code">
        <!-- Row for title & drug type, specification preview -->
        <v-row align="center">
          <v-card-title>
            <h2>{{ drug.name }}</h2>
          </v-card-title>
          <v-spacer></v-spacer>
          <v-chip class="mr-2" color="primary" text-color="white" pill>
            <v-icon small left>mdi-pill</v-icon>
            {{ drug.typeOfDrug }}
          </v-chip>
          <!-- Drug specification -->
          <v-dialog v-model="drug.showSpecification" width="500" :retain-focus="false">
            <template #activator="{ on: dialog }">
              <v-tooltip bottom>
                <template #activator="{ on: tooltip }">
                  <v-btn
                    v-on="{ ...tooltip, ...dialog }"
                    elevation="0"
                    left
                    class="mr-10"
                    fab
                    dark
                    x-small
                    color="light-green lighten-3"
                  >
                    <v-icon dark>mdi-clipboard-file-outline</v-icon>
                  </v-btn>
                </template>
                <span>Drug specification</span>
              </v-tooltip>
            </template>
            <v-card>
              <!--Toolbar of the card-->
              <v-toolbar color="primary" dark dense flat>
                <v-toolbar-title class="body-2">
                  <h3>Drug specification for {{ drug.name }}</h3>
                </v-toolbar-title>
              </v-toolbar>
              <!-- End of toolbar of the card -->
              <br />
              <v-form class="ma-5">
                <v-text-field
                  v-model="drug.drugSpecificationDTO.manufacturer"
                  label="Manufacturer"
                  disabled
                ></v-text-field>
                <v-text-field
                  v-model="drug.drugSpecificationDTO.contraindications"
                  label="Contraindications"
                  disabled
                ></v-text-field>
                <v-text-field
                  v-model="drug.drugSpecificationDTO.recommendedDailyDose"
                  label="Recommended daily dose"
                  disabled
                ></v-text-field>
                <v-select
                  :items="drug.drugSpecificationDTO.ingredients"
                  item-text="name"
                  label="Ingredients"
                ></v-select>
              </v-form>
              <v-card-actions>
                <v-spacer></v-spacer>
                <v-btn
                  color="primary"
                  @click="drug.showSpecification = !drug.showSpecification"
                >Close</v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
          <!-- End of the drug specification -->
        </v-row>
        <!-- End of the row for title & drug type, specification preview -->
        <!-- Row for pharmacies and mark -->
        <v-row>
          <!-- Button to show/hide pharmacies -->
          <v-card-actions>
            <v-btn
              v-if="isPharmacyAdmin && !isInPharmacy(drug)"
              color="success"
              @click="startAddingToPharmacy(drug)"
            >Add to pharmacy</v-btn>
            <v-btn @click="getDrugPharmacies(drug)" color="accent" text>
              Pharmacies
              <v-icon>
                {{
                drug.showPharmacies ? "mdi-chevron-up" : "mdi-chevron-down"
                }}
              </v-icon>
            </v-btn>
          </v-card-actions>
          <!-- End of the button to show/hide pharmacies -->
          <v-spacer></v-spacer>

          <!-- Mark of drug -->
          <v-rating
            class="mr-10"
            v-model="drug.mark"
            color="accent"
            background-color="orange "
            half-increments
            readonly
          ></v-rating>
          <!-- End of mark of the drug -->
          <!-- Pharmacies where is available this drug -->
          <v-expand-transition>
            <div v-show="drug.showPharmacies">
              <v-divider></v-divider>
              <br />
              <v-card
                elevation="4"
                class="pl-5 mr-4 mb-5 ml-16"
                v-for="pharmacy in drug.availableDrugs"
                :key="pharmacy.id"
              >
                <v-card-text>
                  <v-row align="center">
                    <v-col>{{ pharmacy.pharmacyName }}</v-col>

                    <v-col>price: {{ pharmacy.priceOfDrug }} €</v-col>
                    <v-col>
                      <!-- Dialog for making reservation -->
                      <v-dialog
                        v-model="pharmacy.showMakeReservation"
                        width="500"
                        :retain-focus="false"
                      >
                        <template #activator="{ on: dialog }">
                          <v-tooltip bottom>
                            <template #activator="{ on: tooltip }">
                              <v-btn
                                v-if="isPatient()"
                                v-on="{ ...tooltip, ...dialog }"
                                elevation="0"
                                left
                                class="mr-10"
                                fab
                                dark
                                x-small
                                color="light-green lighten-3"
                              >
                                <v-icon dark>mdi-basket</v-icon>
                              </v-btn>
                            </template>
                            <span>Make reservation</span>
                          </v-tooltip>
                        </template>
                        <v-card>
                          <!--Toolbar of the card-->
                          <v-toolbar color="primary" dark dense flat>
                            <v-toolbar-title class="body-2">
                              <h3>Drug reservation</h3>
                            </v-toolbar-title>
                          </v-toolbar>
                          <!-- End of toolbar of the card -->
                          <v-card-subtitle class="pl-5 pt-5">
                            Make reservation for drug {{ drug.name }} in
                            pharmacy {{ pharmacy.pharmacyName }}, where price
                            per drug is {{ pharmacy.priceOfDrug }} €
                          </v-card-subtitle>

                          <v-form class="ma-5">
                            <v-text-field
                              type="date"
                              label="Deadline to pickup drug"
                              v-model="formAmountAndDeadlineDTO.deadline"
                            ></v-text-field>
                            <v-text-field
                              type="number"
                              label="Amount"
                              v-model="formAmountAndDeadlineDTO.amount"
                            ></v-text-field>
                          </v-form>
                          <v-card-actions>
                            <v-spacer></v-spacer>
                            <v-progress-circular
                              class="mr-4"
                              indeterminate
                              color="primary"
                              v-show="performingAction"
                            ></v-progress-circular>
                            <v-btn
                              :disabled="performingAction"
                              color="primary"
                              @click="makeReservation(pharmacy,drug)"
                            >
                              <v-icon dark left>mdi-checkbox-marked-circle</v-icon>Confirm
                            </v-btn>
                          </v-card-actions>
                        </v-card>
                      </v-dialog>
                      <!-- End of dialog for making reservation -->
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
            </div>
          </v-expand-transition>
          <!-- End of the pharmacies where is available this drug -->
        </v-row>
        <!-- End of the row for type of drug and mark -->
      </v-card>
    </v-col>
    <!-- End of the right colum for drugs preview-->

    <v-spacer></v-spacer>

    <!-- Dialog for adding drug to pharmacy -->
    <v-dialog v-model="addingDrug" width="500" :retain-focus="false">
      <v-card>
        <!--Toolbar of the card-->
        <v-toolbar color="primary" dark dense flat>
          <v-toolbar-title class="body-2">
            <h3>Add {{drugToAdd.name}} to the pharmacy</h3>
          </v-toolbar-title>
        </v-toolbar>
        <v-form class="pa-4">
          <v-text-field
            v-model="defaultPrice"
            :error-messages="defaultPriceErrors"
            label="Default price"
            @blur="$v.defaultPrice.$touch()"
            @input="$v.defaultPrice.$touch()"
          ></v-text-field>
        </v-form>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-progress-circular class="mr-4" indeterminate color="primary" v-show="performingAction"></v-progress-circular>
          <v-btn :disabled="performingAction" color="success" @click="addToPharmacy()">
            <v-icon dark left>mdi-checkbox-marked-circle</v-icon>Confirm
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
    <!-- End of dialog for adding drug to pharmacy -->
  </v-row>
</template>

<script>
import { getToken, getParsedToken } from "./../../../util/token";
import {
  getStringDateFromMilliseconds,
  getTodayDateString
} from "./../../../util/dateHandler";
import { validationMixin } from "vuelidate";
import { required, decimal, minValue } from "vuelidate/lib/validators";

export default {
  mixins: [validationMixin],
  validations: {
    defaultPrice: {
      required,
      decimal,
      minValue: minValue(0)
    }
  },
  data: () => ({
    allTypeOfDrugs: [],
    selectedTypeOfDrugs: [],
    markRange: [0, 100],
    marks: ["1", "2", "3", "4", "5"],
    drugs: [],
    searchDrugField: "",
    formAmountAndDeadlineDTO: {
      amount: 0,
      deadline: ""
    },
    isPharmacyAdmin: false,
    drugsInPharmacy: [],
    onlyShowNotInPharm: false,
    snackbarText: "",
    snackbar: false,
    addingDrug: false,
    drugToAdd: { name: "" },
    defaultPrice: "",
    performingAction: false
  }),
  mounted() {
    if (getToken() && getParsedToken().role == "PHARMACY_ADMIN")
      this.isPharmacyAdmin = true;

    this.axios
      .get(
        process.env.VUE_APP_BACKEND_URL + process.env.VUE_APP_ALL_DRUGS_ENDPOINT
      )
      .then(resp => {
        this.drugs = resp.data;
        this.drugs = [];
        for (let drug of resp.data) {
          let tempDrug = {
            name: drug.name,
            alternateDrugs: drug.alternateDrugs,
            code: drug.code,
            loyaltyPoints: drug.loyaltyPoints,
            mark: drug.mark,
            typeOfDrug: drug.typeOfDrug,
            showPharmacies: false,
            showSpecification: false,
            drugSpecificationDTO: {
              manufacturer: drug.drugSpecificationDTO.manufacturer,
              contraindications: drug.drugSpecificationDTO.contraindications,
              ingredients: drug.drugSpecificationDTO.ingredients,
              recommendedDailyDose:
                drug.drugSpecificationDTO.recommendedDailyDose
            }
          };
          this.drugs.push(tempDrug);
        }
      });

    this.axios
      .get(
        process.env.VUE_APP_BACKEND_URL +
          process.env.VUE_APP_ALL_DRUGS_TYPES_ENDPOINT
      )
      .then(resp => {
        this.allTypeOfDrugs = [];
        for (let drugType of resp.data) {
          this.allTypeOfDrugs.push(drugType.name);
        }
      });

    if (this.isPharmacyAdmin) {
      this.axios
        .get(
          process.env.VUE_APP_BACKEND_URL +
            process.env.VUE_APP_DRUGS_IN_PHARMACY_ENDPOINT,
          {
            headers: {
              Authorization: "Bearer " + localStorage.getItem("JWT-CPIS")
            }
          }
        )
        .then(response => {
          this.drugsInPharmacy = [];
          for (let dif of response.data) {
            this.drugsInPharmacy.push(dif.code);
          }
        })
        .catch(error => {
          if (
            error.response &&
            error.response.data &&
            error.response.data.message
          )
            this.snackbarText = error.response.data.message;
          else if (error.message) this.snackbarText = error.message;
          else this.snackbarText = "An unknown error has occured.";
          this.snackbar = true;
        });
    }
  },

  methods: {
    startAddingToPharmacy(drug) {
      this.drugToAdd = drug;
      this.defaultPrice = "";
      this.addingDrug = true;
    },
    addToPharmacy() {
      this.loading = true;

      this.$v.$touch();

      if (this.$v.$invalid) return;

      this.axios
        .post(
          process.env.VUE_APP_BACKEND_URL +
            process.env.VUE_APP_DRUGS_IN_PHARMACY_ENDPOINT,
          {
            code: this.drugToAdd.code,
            defaultPrice: this.defaultPrice
          },
          {
            headers: {
              Authorization: "Bearer " + localStorage.getItem("JWT-CPIS")
            }
          }
        )
        .then(() => {
          this.drugToAdd.showPharmacies = false;
          this.drugToAdd.availableDrugs = null;
          this.drugsInPharmacy.push(this.drugToAdd.code);
          this.snackbarText = "Successfully added drug to pharmacy.";
          this.performingAction = false;
          this.snackbar = true;
          this.addingDrug = false;
          this.getDrugPharmacies(this.drugToAdd);
        })
        .catch(error => {
          if (
            error.response &&
            error.response.data &&
            error.response.data.message
          )
            this.snackbarText = error.response.data.message;
          else if (error.message) this.snackbarText = error.message;
          else this.snackbarText = "An unknown error has occured.";
          this.performingAction = false;
          this.snackbar = true;
        });
    },
    isDateValid() {
      var todayDate = Date.parse(getTodayDateString());
      var deadlineDate = Date.parse(
        getStringDateFromMilliseconds(this.formAmountAndDeadlineDTO.deadline)
      );
      if (todayDate > deadlineDate) return false;
      return true;
    },
    isPatient() {
      if (getToken() && getParsedToken().role == "PATIENT") return true;
      return false;
    },
    makeReservation(pharmacy, drug) {
      if (!this.isDateValid()) {
        alert("The date is past.");
        return;
      }
      if (this.formAmountAndDeadlineDTO.amount < 1) {
        alert("The value of amount must be positive.");
        return;
      } else {
        this.axios
          .post(
            process.env.VUE_APP_BACKEND_URL +
              process.env.VUE_APP_DRUG_RESERVATION_ENDPOINT,
            {
              deadline: this.formAmountAndDeadlineDTO.deadline,
              amount: this.formAmountAndDeadlineDTO.amount,
              patientEmail: getParsedToken().sub,
              drugCode: drug.code,
              pharmacyID: pharmacy.id
            },
            {
              headers: {
                Authorization: "Bearer " + localStorage.getItem("JWT-CPIS")
              }
            }
          )
          .then(() => {
            alert("Successfuly");
          })
          .catch(error => {
            alert("Error: " + error);
          });
      }
    },
    getDrugPharmacies(drug) {
      if (drug.availableDrugs != null) {
        drug.showPharmacies = !drug.showPharmacies;
        return; // PREVENT TO MAKE ONLY ONE AJAX CALL TO GET PHARMACIES
      }
      this.axios
        .post(
          process.env.VUE_APP_BACKEND_URL +
            process.env.VUE_APP_AVAILABLE_DRUGS_ENDPOINT,
          {
            code: drug.code
          }
        )
        .then(response => {
          drug.availableDrugs = [];

          for (let availableDrugTemp of response.data) {
            let tempObj = {
              id: availableDrugTemp.pharmacyID,
              pharmacyName: availableDrugTemp.pharmacyName,
              priceOfDrug: availableDrugTemp.priceOfDrug,
              showMakeReservation: false
            };
            drug.availableDrugs.push(tempObj);
          }
          drug.showPharmacies = !drug.showPharmacies;
        })
        .catch(error => {
          alert("Error: " + error);
          drug.showPharmacies = !drug.showPharmacies;
        });
    },
    isMatchedDrug(drug) {
      // Matched drug is one who match all the criteria
      // Search filter
      if (!drug.name.toLowerCase().match(this.searchDrugField.toLowerCase()))
        return false;

      // Mark filter
      let minMark = this.markRange[0] / 25 + 1;
      let maxMark = this.markRange[1] / 25 + 1;
      if (drug.mark < minMark || drug.mark > maxMark) return false;

      // Type of the drug filter
      if (this.selectedTypeOfDrugs.length != 0) {
        let isSomeOfThoseTypes = false;
        for (let typeOfDrug of this.selectedTypeOfDrugs) {
          if (drug.typeOfDrug == typeOfDrug) {
            isSomeOfThoseTypes = true;
            break;
          }
        }
        if (!isSomeOfThoseTypes) return false;
      }

      if (this.onlyShowNotInPharm && this.isInPharmacy(drug)) return false;

      return true;
    },
    isInPharmacy: function(drug) {
      for (let dif of this.drugsInPharmacy) if (drug.code == dif) return true;
      return false;
    }
  },
  computed: {
    filteredDrugs: function() {
      return this.drugs.filter(drug => {
        return this.isMatchedDrug(drug);
      });
    },
    defaultPriceErrors() {
      const errors = [];
      if (!this.$v.defaultPrice.$dirty) return errors;
      !this.$v.defaultPrice.required &&
        errors.push("Default price is required.");
      !this.$v.defaultPrice.decimal &&
        errors.push("Default price must be a decimal number.");
      !this.$v.defaultPrice.minValue &&
        errors.push("Default price must be positive.");
      return errors;
    }
  }
};
</script>