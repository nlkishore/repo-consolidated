// Example Jenkins declarative stage — CR slice validation
// Requires: CR_MANIFEST_PATH, CR_SLICE_REF, Python 3.10+ on agent
// See: docs/review/CR_SLICE_REVIEW_AUTOMATION_DESIGN.md

stage('Validate CR slice') {
    steps {
        dir('git-tools/main/modules/python') {
            sh '''
                pip install -r requirements-cr-slice-validation.txt
                python -m cr_slice_validation run-all \
                  --manifest "${WORKSPACE}/${CR_MANIFEST_PATH}" \
                  --repo "${WORKSPACE}" \
                  --slice-ref "${CR_SLICE_REF}" \
                  --output "${WORKSPACE}/reconcile-report.json"
            '''
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'reconcile-report.json', allowEmptyArchive: false
        }
        failure {
            echo 'CR slice reconciliation FAILED — see reconcile-report.json'
        }
    }
}
