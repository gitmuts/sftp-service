var home = Vue.component("Home", {
    template: `<div>
            <v-snackbar v-model="snackbar" :right='true' :timeout="timeout" :top='true' :color="snackColor">
              {{ message }}
            <v-btn color="primary" flat  @click="snackbar = false">
              Close
            </v-btn>
            </v-snackbar>
       <v-layout row wrap>
       <v-flex xs12>
        <v-card-actions>
            <v-spacer> </v-spacer>
            <v-btn color="primary" @click="openSendFilesDialog"> Send Files </v-btn>
        </v-card-actions>
       </v-flex>
       <v-flex xs12>
        <v-card-actions>
            <v-progress-circular v-show="loading" indeterminate color="primary"></v-progress-circular>
            <v-spacer> </v-spacer>
            <v-flex xs4>
            <v-text-field
                v-model="search"
                append-icon="search"
                label="Search"
                single-line
                hide-details
            ></v-text-field>
            </v-flex>
        </v-card-actions>
       </v-flex>
       <v-flex xs12>
        <v-data-table
            :headers="headers"
            :items="items"
            :pagination.sync="pagination"
            :search="search"
            class="elevation-1"
            >
            <template v-slot:items="props">
                <td>{{ props.item.id }}</td>
                <td>{{ props.item.branch }}</td>
                <td :class="props.item.status === 'FAILED' ? 'error--text': ''">{{ props.item.status }}</td>
                <td>{{ props.item.desc }}</td>
                <td>{{ props.item.createdAt }}</td>
                <td>{{ props.item.updatedAt }}</td>
                <td>
                 <v-btn color="info" @click="confirmDialog(props.item)">RESEND</v-btn>
                </td>
            </template>
            </v-data-table>
          </v-flex>
          <v-dialog v-model="dialog" persistent max-width="400">
                <v-card>
                <v-card-title class="headline #162614--text">Confirm File Resend</v-card-title>
                <v-card-text>
                   <p style="font-weight: 600"> Resend File to {{ record.branch }} ? </p>
                </v-card-text>
                <v-card-actions>
                    <v-spacer></v-spacer>
                    <v-btn color="green darken-1" flat @click="dialog = false">Cancel</v-btn>
                    <v-btn color="green darken-1" flat @click="resendFile(record)">Confirm</v-btn>
                </v-card-actions>
                </v-card>
         </v-dialog>
         <v-dialog v-model="sendFilesDialog" persistent max-width="500">
                <v-card>
                <v-card-title class="headline">Confirm Send Files</v-card-title>
                <v-card-text>
                  Start send file process ?
                </v-card-text>
                <v-card-actions>
                    <v-btn  flat class="#00513B--text" @click="sendFilesDialog = false">Cancel</v-btn>
                    <v-spacer></v-spacer>
                    <v-btn color="#7BC243" class="white--text" @click="sendFiles">Confirm</v-btn>
                </v-card-actions>
                </v-card>
         </v-dialog>
       </v-layout>
     </div>`,
    data() {
      return {
        headers: [
            { text: 'Id', value: 'id'},
            { text: 'Branch', value: 'branch' },
            { text: 'Status', value: 'status' },
            { text: 'Desc', value: 'desc' },
            { text: 'Created At', value: 'createdAt' },
            { text: 'Updated At', value: 'updatedAt' }
          ],
          items: [],
          pagination: {sortBy: 'id', descending: true},
          search: '',
          dialog: false,
          record: {},
          snackbar: false,
          snackColor: '',
          message: '',
          timeout: 6000,
          loading: false,
          sendFilesDialog: false
      };
    },
    mounted () {
        this.getSentFiles();
    },
    methods: {
        sendFiles () {
            this.loading = true;
            this.sendFilesDialog = false
            let endpoint="/sendfiles";
            axios({
                method: 'get',
                url: endpoint,
                }).then(response => {
                this.loading = false
                console.log(response)
                this.getSentFiles();
                }).catch(err => {
                this.loading = false
                console.err('err', err)
                })
        },
        getSentFiles () {
            let endpoint="/getrecords";
            axios({
                method: 'get',
                url: endpoint,
                }).then(response => {
                console.log(response)
                this.items = response.data
                }).catch(err => {
                console.err('err', err)
                })
        },
        confirmDialog (item) {
            this.record = item
            this.dialog = true
        },
        openSendFilesDialog () {
            this.sendFilesDialog = true
        },
        resendFile (record) {
            this.dialog = false
            this.loading = true
            let endpoint="/resendfile";
            axios({
                method: 'post',
                data: record,
                url: endpoint,
                }).then(response => {
                this.loading = false
                console.log(response)
                this.message = response.data
                this.snackbar = true
                this.snackColor = 'success'
                }).catch(err => {
                this.loading = false
                console.err('err', err)
                this.message = 'File not resend, check logs'
                this.snackbar = true
                this.snackColor = 'error'
                })
        }
    }
  });