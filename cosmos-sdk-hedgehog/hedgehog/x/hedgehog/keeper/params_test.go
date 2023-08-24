package keeper_test

import (
	"testing"

	"github.com/stretchr/testify/require"
	testkeeper "hedgehog/testutil/keeper"
	"hedgehog/x/hedgehog/types"
)

func TestGetParams(t *testing.T) {
	k, ctx := testkeeper.HedgehogKeeper(t)
	params := types.DefaultParams()

	k.SetParams(ctx, params)

	require.EqualValues(t, params, k.GetParams(ctx))
}
