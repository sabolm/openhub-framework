import { toastr } from 'react-redux-toastr'
import {
  fetchMessage,
  restartMessage,
  cancelMessage
} from '../../../services/messages.service'

// ------------------------------------
// Constants
// ------------------------------------
export const GET_MESSAGE_SUCCESS = 'GET_MESSAGE_SUCCESS'

// ------------------------------------
// Actions
// ------------------------------------

export const getMessageSuccess = (payload) => ({
  type: GET_MESSAGE_SUCCESS,
  payload
})

export const getMessage = (id) => (dispatch) => {
  fetchMessage(id)
    .then((data) => dispatch(getMessageSuccess(data)))
}

export const restart = (id, total) =>
  () => {
    toastr.confirm('Are you sure that you want to restart this message?', {
      onOk: () => {
        restartMessage(id, total)
          .then((data) => {
            if (data.result === 'OK') {
              toastr.success(data.resultDescription)
            } else {
              toastr.error(data.resultDescription)
            }
          })
          .catch(() => {
            toastr.error('Message restart failed')
          })
      }
    })
  }

export const cancel = (id) =>
  () => {
    toastr.confirm('Are you sure that you want to cancel this message?', {
      onOk: () => {
        cancelMessage(id)
          .then((data) => {
            if (data.result === 'OK') {
              toastr.success(data.resultDescription)
            } else {
              toastr.error(data.resultDescription)
            }
          })
          .catch(() => {
            toastr.error('Message cancelation failed')
          })
      }
    })
  }

export const actions = {
  getMessage,
  restart,
  cancel
}

// ------------------------------------
// Action Handlers
// ------------------------------------
const ACTION_HANDLERS = {
  [GET_MESSAGE_SUCCESS]: (state, { payload }) => ({ ...state, message: payload })
}

// ------------------------------------
// Reducer
// ------------------------------------
const initialState = {
  message: null
}

export default function (state = initialState, action) {
  const handler = ACTION_HANDLERS[action.type]
  return handler ? handler(state, action) : state
}
