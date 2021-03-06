import { pipe, toPairs, map } from 'ramda'
import { fetchLoggers, editLogger } from '../../../services/loggers.service'

// ------------------------------------
// Constants
// ------------------------------------
export const GET_LOGGERS_SUCCESS = 'GET_LOGGERS_SUCCESS'
export const GET_LOGGERS_ERROR = 'GET_LOGGERS_ERROR'

// ------------------------------------
// Transforms
// ------------------------------------
const transformLoggers = pipe(
  toPairs,
  map((i) => ({
    name: i[0],
    data: i[1]
  }))
)

// ------------------------------------
// Actions
// ------------------------------------

export const getLoggers = () => (dispatch) => {
  return fetchLoggers()
    .then((data) => {
      const loggingData = {
        levels: data.levels,
        loggers: transformLoggers(data.loggers)
      }
      dispatch({ type: GET_LOGGERS_SUCCESS, payload: loggingData })
    })
    .catch(() => {
      dispatch({ type: GET_LOGGERS_ERROR })
    })
}

export const updateLogger = (logger, configuredLevel) => (dispatch) => {
  return editLogger(logger, configuredLevel)
    .then(() => {
      dispatch(getLoggers())
    })
}

export const actions = {
  getLoggers
}

// ------------------------------------
// Action Handlers
// ------------------------------------
const ACTION_HANDLERS = {
  [GET_LOGGERS_SUCCESS]: (state, { payload }) => ({ ...state, loggingData: payload })
}

// ------------------------------------
// Reducer
// ------------------------------------
export const initialState = {
  loggingData: null
}

export default function (state = initialState, action) {
  const handler = ACTION_HANDLERS[action.type]
  return handler ? handler(state, action) : state
}
